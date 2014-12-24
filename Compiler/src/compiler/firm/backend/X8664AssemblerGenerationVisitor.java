package compiler.firm.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.firm.FirmUtils;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.bit32.AddlOperation;
import compiler.firm.backend.operations.bit32.CltdOperation;
import compiler.firm.backend.operations.bit32.CmpOperation;
import compiler.firm.backend.operations.bit32.CondJumpOperation;
import compiler.firm.backend.operations.bit32.DivOperation;
import compiler.firm.backend.operations.bit32.ImullOperation;
import compiler.firm.backend.operations.bit32.JumpOperation;
import compiler.firm.backend.operations.bit32.MovlOperation;
import compiler.firm.backend.operations.bit32.NegatelOperation;
import compiler.firm.backend.operations.bit32.ShllOperation;
import compiler.firm.backend.operations.bit32.SublOperation;
import compiler.firm.backend.operations.bit64.AddqOperation;
import compiler.firm.backend.operations.bit64.CallOperation;
import compiler.firm.backend.operations.bit64.MovqOperation;
import compiler.firm.backend.operations.bit64.PopqOperation;
import compiler.firm.backend.operations.bit64.PushqOperation;
import compiler.firm.backend.operations.bit64.RetOperation;
import compiler.firm.backend.operations.bit64.ShlqOperation;
import compiler.firm.backend.operations.bit64.SubqOperation;
import compiler.firm.backend.operations.general.Comment;
import compiler.firm.backend.operations.general.LabelOperation;
import compiler.firm.backend.operations.general.SizeOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.StackPointer;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.Mode;
import firm.nodes.Add;
import firm.nodes.Address;
import firm.nodes.Align;
import firm.nodes.Alloc;
import firm.nodes.Anchor;
import firm.nodes.And;
import firm.nodes.Bad;
import firm.nodes.Bitcast;
import firm.nodes.Block;
import firm.nodes.Builtin;
import firm.nodes.Call;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Confirm;
import firm.nodes.Const;
import firm.nodes.Conv;
import firm.nodes.CopyB;
import firm.nodes.Deleted;
import firm.nodes.Div;
import firm.nodes.Dummy;
import firm.nodes.End;
import firm.nodes.Eor;
import firm.nodes.Free;
import firm.nodes.IJmp;
import firm.nodes.Id;
import firm.nodes.Jmp;
import firm.nodes.Load;
import firm.nodes.Member;
import firm.nodes.Minus;
import firm.nodes.Mod;
import firm.nodes.Mul;
import firm.nodes.Mulh;
import firm.nodes.Mux;
import firm.nodes.NoMem;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Offset;
import firm.nodes.Or;
import firm.nodes.Phi;
import firm.nodes.Pin;
import firm.nodes.Proj;
import firm.nodes.Raise;
import firm.nodes.Return;
import firm.nodes.Sel;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Size;
import firm.nodes.Start;
import firm.nodes.Store;
import firm.nodes.Sub;
import firm.nodes.Switch;
import firm.nodes.Sync;
import firm.nodes.Tuple;
import firm.nodes.Unknown;

public class X8664AssemblerGenerationVisitor implements BulkPhiNodeVisitor {

	private static final int STACK_ITEM_SIZE = 8;

	private final List<AssemblerOperation> operations = new ArrayList<>();
	private final HashMap<String, CallingConvention> callingConventions;

	// stack management
	private final HashMap<Node, Storage> nodeStorages = new HashMap<>();
	private int currentStackOffset;
	private Block currentBlock;

	private List<Phi> phis;

	public X8664AssemblerGenerationVisitor(HashMap<String, CallingConvention> callingConventions) {
		this.callingConventions = callingConventions;
	}

	public List<AssemblerOperation> getOperations() {
		return operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	private boolean is64bitNode(Node node) {
		return node.getMode().equals(FirmUtils.getModeReference());
	}

	private void getValue(Node node, Register register) {
		addOperation(new Comment("restore from stack"));

		// if variable was assigned, than simply load it from stack

		if (nodeStorages.containsKey(node)) {
			getValue(node, register, getStorage(node));

		} else { // The value has not been set yet. Reserve memory for it. TODO: check if this is a valid case
			StackPointer stackOffset = reserveStackItem();
			nodeStorages.put(node, stackOffset);

		}
	}

	private Storage getStorage(Node node) {
		return nodeStorages.get(node);
	}

	private void getValue(Node node, Register register, Storage stackPointer) {
		if (is64bitNode(node)) {
			addOperation(new MovqOperation("Load address " + node.toString(), stackPointer, register));
		} else {
			addOperation(new MovlOperation("Load node " + node.toString(), stackPointer, register));
		}
	}

	private StackPointer storeValueOnNewStackPointer(Node node, Storage storage) {
		// Allocate stack
		StackPointer stackPointer = reserveStackItem();

		storeValue(node, storage, stackPointer);
		return stackPointer;
	}

	private StackPointer reserveStackItem() {
		currentStackOffset -= STACK_ITEM_SIZE;
		addOperation(new SubqOperation("Increment stack size", new Constant(STACK_ITEM_SIZE), Register.RSP));
		return new StackPointer(currentStackOffset, Register.RBP);
	}

	private void storeValue(Node node, Storage storage) {
		Storage stackPointer = nodeStorages.get(node);
		if (stackPointer == null) {
			stackPointer = storeValueOnNewStackPointer(node, storage);
			nodeStorages.put(node, stackPointer);
		} else {
			storeValue(node, storage, stackPointer);
		}
	}

	private void storeValue(Node node, Storage storage, Storage stackPointer) {
		if (is64bitNode(node)) {
			addOperation(new MovqOperation("Store node " + node, storage, stackPointer));
		} else {
			addOperation(new MovlOperation("Store node " + node, storage, stackPointer));
		}
	}

	private <T extends StorageRegisterOperation> void visitTwoOperandsNode(T operation, Node parent, Node left, Node right) {
		// move left node to RAX
		getValue(left, Register.EAX);
		// move right node to RBX
		getValue(right, Register.EDX);
		// TODO: find a nicer way to instantiate T directly instead of passing an instance and then initializing
		operation.initialize(Register.EAX, Register.EDX);
		// execute operation on RAX, RBX
		addOperation(operation);
		// store on stack
		storeValue(parent, Register.EDX);
	}

	private String getBlockLabel(Block node) {
		return "BLOCK_" + node.getNr();
	}

	public void addListOfAllPhis(List<Phi> phis) {
		this.phis = phis;
	}

	public void reserveMemoryForPhis() {
		addOperation(new Comment("Reserve space for phis"));
		for (Phi phi : phis) {
			nodeStorages.put(phi, reserveStackItem());
		}
	}

	// ----------------------------------------------- NodeVisitor ---------------------------------------------------

	@Override
	public void visit(Add node) {
		if (node.getMode().equals(FirmUtils.getModeReference())) {
			visitTwoOperandsNode(new AddqOperation("add operation"), node, node.getLeft(), node.getRight());
		} else {
			visitTwoOperandsNode(new AddlOperation("add operation"), node, node.getLeft(), node.getRight());
		}
	}

	@Override
	public void visit(Address node) {
		// This is handled in a call.
	}

	@Override
	public void visit(Align node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Alloc node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Anchor node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(And node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Bad node) {
		// Ignore Bad nodes.
	}

	@Override
	public void visit(Bitcast node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Block node) {
		currentBlock = node;
		addOperation(new Comment(node.toString()));

		Graph graph = node.getGraph();
		String methodName = graph.getEntity().getLdName();

		// we are in start block: initialize stack (RBP)
		if (node.equals(graph.getStartBlock())) {
			addOperation(new LabelOperation(methodName));

			addOperation(new PushqOperation(Register.RBP)); // Dynamic Link
			addOperation(new MovqOperation(Register.RSP, Register.RBP));

			reserveMemoryForPhis();
		}

		if (node.equals(graph.getEndBlock())) {
			if (!Utils.isWindows()) {
				addOperation(new SizeOperation(methodName));
			}
		}

		// prepend a label before each block
		addOperation(new LabelOperation(getBlockLabel(node)));
	}

	@Override
	public void visit(Builtin node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Call node) {
		int predCount = node.getPredCount();
		if (predCount >= 2 && node.getPred(1) instanceof Address) { // Minimum for all calls
			int firmOffset = 2;
			int parametersCount = (predCount - firmOffset);
			Address callAddress = (Address) node.getPred(1);
			String methodName = callAddress.getEntity().getLdName();

			addOperation(new Comment("Call " + methodName + " " + node.getNr()));

			CallingConvention callingConvention = CallingConvention.SYSTEMV_ABI;
			if (callingConventions.containsKey(methodName)) {
				callingConvention = callingConventions.get(methodName);
			}

			for (AssemblerOperation operation : callingConvention.getPrefixOperations()) {
				addOperation(operation);
			}

			Register[] callingRegisters = callingConvention.getParameterRegisters();

			// The following is before filling registers to have no problem with register allocation.
			int remainingParameters = parametersCount - callingRegisters.length;
			firmOffset += callingRegisters.length;
			Constant parameterSize = new Constant(STACK_ITEM_SIZE * remainingParameters);

			if (remainingParameters > 0) {
				addOperation(new SubqOperation(parameterSize, Register.RSP));

				for (int i = 0; i < remainingParameters; i++) {
					Storage sourcePointer = getStorage(node.getPred(i + firmOffset));
					// Copy parameter
					Register temporaryRegister = Register.EAX;
					StackPointer destinationPointer = new StackPointer(i * STACK_ITEM_SIZE, Register.RSP);
					if (node.getPred(i + firmOffset) instanceof Proj) {
						addOperation(new MovqOperation(sourcePointer, temporaryRegister));
						addOperation(new MovqOperation(temporaryRegister, destinationPointer));
					} else {
						addOperation(new MovlOperation(sourcePointer, temporaryRegister));
						addOperation(new MovlOperation(temporaryRegister, destinationPointer));
					}
				}
			}
			firmOffset -= callingRegisters.length;

			// TODO: Register Allocation: Possible save all conflict registers.

			for (int i = 0; i < parametersCount && i < callingRegisters.length; i++) {
				// Copy parameters in registers
				Node parameterNode = node.getPred(i + firmOffset);
				getValue(parameterNode, callingRegisters[i]);
			}

			addOperation(new CallOperation(methodName));

			if (remainingParameters > 0) {
				addOperation(new AddqOperation(parameterSize, Register.RSP));
			}

			for (AssemblerOperation operation : callingConvention.getSuffixOperations()) {
				addOperation(operation);
			}

			// TODO: Check if this also works for pointers
			for (Edge edge : BackEdges.getOuts(node)) {
				if (edge.node.getMode().equals(Mode.getT())) {
					for (Edge innerEdge : BackEdges.getOuts(edge.node)) {
						storeValue(innerEdge.node, callingConvention.getReturnRegister());
					}
				}
			}

		}

	}

	@Override
	public void visit(Cmp node) {
		// Nothing to do here, its handled in Cond.
	}

	@Override
	public void visit(Cond node) {
		Cmp cmpNode = (Cmp) node.getPred(0);
		Block blockTrue = null;
		Block blockFalse = null;

		// get blocks the cond node shows to
		// TODO: Refactore me!
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (edgeNode instanceof Proj) {
				boolean trueCase = ((Proj) edgeNode).getNum() == FirmUtils.TRUE;
				for (Edge nextEdge : BackEdges.getOuts(edgeNode)) {
					Node nextEdgeNode = nextEdge.node;
					if (nextEdgeNode instanceof Block) {
						if (trueCase) {
							blockTrue = (Block) nextEdgeNode;
						} else {
							blockFalse = (Block) nextEdgeNode;
						}
					}
				}
			}
		}

		// generate cmp instruction
		visitCmpNode(cmpNode);

		String labelTrue = getBlockLabel(blockTrue);
		String labelFalse = getBlockLabel(blockFalse);

		// now add conditional jump
		switch (cmpNode.getRelation()) {
		case Equal:
			addOperation(CondJumpOperation.createJumpZero(labelTrue));
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case False:
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case Greater:
			addOperation(CondJumpOperation.createJumpGreater(labelTrue));
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case GreaterEqual:
			addOperation(CondJumpOperation.createJumpGreaterEqual(labelTrue));
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case Less:
			addOperation(CondJumpOperation.createJumpLess(labelTrue));
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case LessEqual:
			addOperation(CondJumpOperation.createJumpLessEqual(labelTrue));
			addOperation(CondJumpOperation.createJump(labelFalse));
			break;
		case LessEqualGreater:
		case LessGreater:
			addOperation(CondJumpOperation.createJumpZero(labelFalse));
			addOperation(CondJumpOperation.createJump(labelTrue));
			break;
		case True:
			addOperation(CondJumpOperation.createJump(labelTrue));
			break;
		case Unordered:
		case UnorderedEqual:
		case UnorderedGreater:
		case UnorderedGreaterEqual:
		case UnorderedLess:
		case UnorderedLessEqual:
		case UnorderedLessGreater:
		default:
			throw new RuntimeException("No unordered relations available, tried " + cmpNode.getRelation());
		}
	}

	@Override
	public void visit(Confirm node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Const node) {
		// nothing to do
		nodeStorages.put(node, new Constant(node));
	}

	@Override
	public void visit(Conv node) {
		if (node.getPredCount() >= 1) {
			// TODO: This should maybe optimized with register allocation.
			getValue(node.getPred(0), Register.EAX);
			storeValue(node, Register.EAX);
		}
	}

	@Override
	public void visit(CopyB node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Deleted node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	private void visitDivMod(Node node, Node left, Node right, Register storeRegister) {
		addOperation(new Comment("div operation"));
		// move left node to EAX
		getValue(left, Register.EAX);
		// move right node to RBX
		getValue(right, Register.ESI);
		addOperation(new CltdOperation());
		// idivl (eax / esi)
		addOperation(new DivOperation(Register.ESI));
		// store on stack
		for (Edge edge : BackEdges.getOuts(node)) {
			storeValue(edge.node, storeRegister);
		}
	}

	@Override
	public void visit(Div node) {
		visitDivMod(node, node.getLeft(), node.getRight(), Register.EAX);
	}

	@Override
	public void visit(Dummy node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(End node) {
		addOperation(new Comment("end node"));
	}

	@Override
	public void visit(Eor node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Free node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(IJmp node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Id node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Jmp node) {
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (edgeNode instanceof Block) {
				addOperation(JumpOperation.createJump(getBlockLabel((Block) edgeNode)));
				break;
			}
		}
	}

	@Override
	public void visit(Load node) {
		Node referenceNode = node.getPred(1);
		getValue(referenceNode, Register.EAX);
		addOperation(new MovqOperation(new StackPointer(0, Register.EAX), Register.EAX));
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				storeValue(edgeNode, Register.EAX);
			}
		}
	}

	@Override
	public void visit(Member node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Minus node) {
		getValue(node.getPred(0), Register.EAX);
		addOperation(new NegatelOperation(Register.EAX));
		storeValue(node, Register.EAX);
	}

	@Override
	public void visit(Mod node) {
		visitDivMod(node, node.getLeft(), node.getRight(), Register.EDX);
	}

	@Override
	public void visit(Mul node) {
		visitTwoOperandsNode(new ImullOperation("mul operation"), node, node.getRight(), node.getLeft());
	}

	@Override
	public void visit(Mulh node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Mux node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(NoMem node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Not node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Offset node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Or node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Phi phi) {
		throw new RuntimeException("Phis are visited in visit(List<Phi>)");
	}

	@Override
	public void visit(Pin node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	private void visitCmpNode(Cmp node) {
		getValue(node.getRight(), Register.EAX);
		getValue(node.getLeft(), Register.EDX);
		addOperation(new CmpOperation("cmp operation", Register.EAX, Register.EDX));
	}

	@Override
	public void visit(Proj node) {
		if (node.getPredCount() == 1 && node.getPred(0) instanceof Start && node.getMode().equals(Mode.getT())) {
			for (Edge edge : BackEdges.getOuts(node)) {
				if (edge.node instanceof Proj) {
					Proj proj = (Proj) edge.node;
					nodeStorages.put(proj, new StackPointer(STACK_ITEM_SIZE * (proj.getNum() + 2), Register.RBP)); // + 2 for dynamic link
				}
			}
		}
	}

	@Override
	public void visit(Raise node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Return node) {
		addOperation(new Comment("restore stack size"));
		if (node.getPredCount() > 1) {
			// Store return value in EAX register
			getValue(node.getPred(1), Register.EAX);
		}

		// addOperation(node.getBlock(), new AddqOperation(new Constant(-currentStackOffset), Register.RSP));
		// better move rbp to rsp
		addOperation(new MovqOperation(Register.RBP, Register.RSP));
		addOperation(new PopqOperation(Register.RBP));
		addOperation(new RetOperation());
		currentStackOffset = 0;

	}

	@Override
	public void visit(Sel node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Shl node) {
		// move left node to a register
		getValue(node.getLeft(), Register.EAX);

		AssemblerOperation operation;
		Constant constant = new Constant((Const) node.getRight());

		if (node.getMode().equals(FirmUtils.getModeReference())) {
			operation = new ShlqOperation(Register.EAX, constant);
		} else {
			operation = new ShllOperation(Register.EAX, constant);
		}
		// execute operation
		addOperation(operation);
		// store on stack
		storeValue(node, Register.EAX);
	}

	@Override
	public void visit(Shr node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Shrs node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Size node) {
	}

	@Override
	public void visit(Start node) {
	}

	@Override
	public void visit(Store node) {
		Node source = node.getPred(1);
		getValue(source, Register.EAX);
		getValue(node.getPred(2), Register.ECX);
		addOperation(new MovlOperation(Register.ECX, new StackPointer(0, Register.EAX)));
	}

	@Override
	public void visit(Sub node) {
		// we subtract the right node from the left, not the otherway around
		visitTwoOperandsNode(new SublOperation("sub operation"), node, node.getRight(), node.getLeft());
	}

	@Override
	public void visit(Switch node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Sync node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Tuple node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Unknown node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visitUnknown(Node node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	private Node getRelevantPredecessor(Phi phi) {
		Node phiBlock = phi.getBlock();

		for (int i = 0; i < phiBlock.getPredCount(); i++) {
			Node blockPredecessors = phiBlock.getPred(i);
			if (blockPredecessors.getBlock().getNr() == currentBlock.getNr()) {
				return phi.getPred(i);
			}
		}
		return null;
	}

	@Override
	public void visit(List<Phi> phis) {
		addOperation(new Comment("Handle phis of current block"));
		HashMap<Phi, StackPointer> phiTempStackMapping = new HashMap<>();
		for (Phi phi : phis) {
			Node predecessor = getRelevantPredecessor(phi);
			getValue(predecessor, Register.EAX);
			StackPointer stackPointer = storeValueOnNewStackPointer(predecessor, Register.EAX);
			phiTempStackMapping.put(phi, stackPointer);
		}

		for (Phi phi : phis) {
			getValue(phi, Register.EAX, phiTempStackMapping.get(phi));
			storeValue(phi, Register.EAX);
		}
	}
}
