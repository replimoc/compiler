package compiler.firm.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.firm.FirmUtils;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.CltdOperation;
import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.DivOperation;
import compiler.firm.backend.operations.ImulOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.NegOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.RetOperation;
import compiler.firm.backend.operations.ShlOperation;
import compiler.firm.backend.operations.SizeOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.jump.JgOperation;
import compiler.firm.backend.operations.jump.JgeOperation;
import compiler.firm.backend.operations.jump.JlOperation;
import compiler.firm.backend.operations.jump.JleOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.jump.JzOperation;
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

	private void getValue(Node node, Register register) {
		addOperation(new Comment("restore from stack"));

		// if variable was assigned, than simply load it from stack

		if (!nodeStorages.containsKey(node)) {
			// The value has not been set yet. Reserve memory for it. TODO: check if this is a valid case
			StackPointer stackOffset = reserveStackItem();
			nodeStorages.put(node, stackOffset);
			addOperation(new Comment("expected " + node + " to be on stack"));

		}
		getValue(node, register, getStorage(node));
	}

	private Storage getStorage(Node node) {
		return nodeStorages.get(node);
	}

	private void getValue(Node node, Register register, Storage stackPointer) {
		if (getMode(node) == Bit.BIT8) {
			addOperation(new MovOperation("movb does not clear the register before write", Bit.BIT64, new Constant(0), register));
		}
		addOperation(new MovOperation("Load address " + node.toString(), getMode(node), stackPointer, register));
	}

	private StackPointer storeValueOnNewStackPointer(Node node, Storage storage) {
		// Allocate stack
		StackPointer stackPointer = reserveStackItem();

		storeValue(node, storage, stackPointer);
		return stackPointer;
	}

	private StackPointer reserveStackItem() {
		currentStackOffset -= STACK_ITEM_SIZE;
		return new StackPointer(currentStackOffset, Register._BP);
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
		addOperation(new MovOperation("Store node " + node, getMode(node), storage, stackPointer));
	}

	private <T extends StorageRegisterOperation> void visitTwoOperandsNode(T operation, Node parent, Node left, Node right) {
		// move left node to RAX
		getValue(left, Register._AX);
		// move right node to RBX
		getValue(right, Register._DX);
		// TODO: find a nicer way to instantiate T directly instead of passing an instance and then initializing
		operation.initialize(Register._AX, Register._DX);
		// execute operation on RAX, RBX
		addOperation(operation);
		// store on stack
		storeValue(parent, Register._DX);
	}

	private LabelOperation getBlockLabel(Block node) {
		return new LabelOperation("BLOCK_" + node.getNr());
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

	private Bit getMode(Node node) {
		if (node.getMode().equals(FirmUtils.getModeReference())) {
			return Bit.BIT64;
		} else if (node.getMode().equals(FirmUtils.getModeBoolean())) {
			return Bit.BIT8;
		} else {
			return Bit.BIT32;
		}
	}

	// ----------------------------------------------- NodeVisitor ---------------------------------------------------

	@Override
	public void visit(Add node) {
		visitTwoOperandsNode(new AddOperation("add operation", getMode(node)), node, node.getLeft(), node.getRight());
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

			addOperation(new PushOperation(Bit.BIT64, Register._BP)); // Dynamic Link
			addOperation(new MovOperation(Bit.BIT64, Register._SP, Register._BP));

			// FIXME: static stack reservation is no solution
			addOperation(new SubOperation("static stack reservation", Bit.BIT64, new Constant(STACK_ITEM_SIZE * 64), Register._SP));

			reserveMemoryForPhis();
		}

		if (node.equals(graph.getEndBlock())) {
			addOperation(new AddOperation("free stack", Bit.BIT64, new Constant(STACK_ITEM_SIZE * 64), Register._SP));
			currentStackOffset = 0;
			if (!Utils.isWindows()) {
				addOperation(new SizeOperation(methodName));
			}
		}

		// prepend a label before each block
		addOperation(getBlockLabel(node));
	}

	@Override
	public void visit(Builtin node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Call node) {
		int predCount = node.getPredCount();
		assert predCount >= 2 && node.getPred(1) instanceof Address : "Minimum for all calls";

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
			addOperation(new SubOperation(Bit.BIT64, parameterSize, Register._SP));

			for (int i = 0; i < remainingParameters; i++) {
				Storage sourcePointer = getStorage(node.getPred(i + firmOffset));
				// Copy parameter
				Register temporaryRegister = Register._AX;
				StackPointer destinationPointer = new StackPointer(i * STACK_ITEM_SIZE, Register._SP);
				Bit mode = getMode(node.getPred(i + firmOffset));
				addOperation(new MovOperation(mode, sourcePointer, temporaryRegister));
				addOperation(new MovOperation(mode, temporaryRegister, destinationPointer));
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
			addOperation(new AddOperation(Bit.BIT64, parameterSize, Register._SP));
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

		LabelOperation labelTrue = getBlockLabel(blockTrue);
		LabelOperation labelFalse = getBlockLabel(blockFalse);

		// now add conditional jump
		switch (cmpNode.getRelation()) {
		case Equal:
			addOperation(new JzOperation(labelTrue));
			addOperation(new JmpOperation(labelFalse));
			break;
		case False:
			addOperation(new JmpOperation(labelFalse));
			break;
		case Greater:
			addOperation(new JgOperation(labelTrue));
			addOperation(new JmpOperation(labelFalse));
			break;
		case GreaterEqual:
			addOperation(new JgeOperation(labelTrue));
			addOperation(new JmpOperation(labelFalse));
			break;
		case Less:
			addOperation(new JlOperation(labelTrue));
			addOperation(new JmpOperation(labelFalse));
			break;
		case LessEqual:
			addOperation(new JleOperation(labelTrue));
			addOperation(new JmpOperation(labelFalse));
			break;
		case LessEqualGreater:
		case LessGreater:
			addOperation(new JzOperation(labelFalse));
			addOperation(new JmpOperation(labelTrue));
			break;
		case True:
			addOperation(new JmpOperation(labelTrue));
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
		assert node.getPredCount() >= 1 : "Conv nodes should have a predecessor";

		// TODO: This should maybe optimized with register allocation.
		getValue(node.getPred(0), Register._AX);
		storeValue(node, Register._AX);
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
		getValue(left, Register._AX);
		// move right node to RSI
		getValue(right, Register._SI);
		addOperation(new CltdOperation());
		// idivl (eax / esi)
		addOperation(new DivOperation(getMode(right), Register._SI));
		// store on stack
		for (Edge edge : BackEdges.getOuts(node)) {
			storeValue(edge.node, storeRegister);
		}
	}

	@Override
	public void visit(Div node) {
		visitDivMod(node, node.getLeft(), node.getRight(), Register._AX);
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
				addOperation(new JmpOperation(getBlockLabel((Block) edgeNode)));
				break;
			}
		}
	}

	@Override
	public void visit(Member node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Minus node) {
		getValue(node.getPred(0), Register._AX);
		addOperation(new NegOperation(getMode(node), Register._AX));
		storeValue(node, Register._AX);
	}

	@Override
	public void visit(Mod node) {
		visitDivMod(node, node.getLeft(), node.getRight(), Register._DX);
	}

	@Override
	public void visit(Mul node) {
		visitTwoOperandsNode(new ImulOperation("mul operation", getMode(node)), node, node.getRight(), node.getLeft());
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
		getValue(node.getRight(), Register._AX);
		getValue(node.getLeft(), Register._DX);
		addOperation(new CmpOperation("cmp operation", getMode(node), Register._AX, Register._DX));
	}

	@Override
	public void visit(Proj node) {
		if (node.getPredCount() == 1 && node.getPred(0) instanceof Start && node.getMode().equals(Mode.getT())) {
			for (Edge edge : BackEdges.getOuts(node)) {
				if (edge.node instanceof Proj) {
					Proj proj = (Proj) edge.node;
					nodeStorages.put(proj, new StackPointer(STACK_ITEM_SIZE * (proj.getNum() + 2), Register._BP)); // + 2 for dynamic link
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
			getValue(node.getPred(1), Register._AX);
		}

		// addOperation(node.getBlock(), new AddqOperation(new Constant(-currentStackOffset), Register.RSP));
		// better move rbp to rsp
		addOperation(new MovOperation(Bit.BIT64, Register._BP, Register._SP));
		addOperation(new PopOperation(Bit.BIT64, Register._BP));
		addOperation(new RetOperation());
	}

	@Override
	public void visit(Sel node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Shl node) {
		// move left node to a register
		getValue(node.getLeft(), Register._AX);

		Constant constant = new Constant((Const) node.getRight());

		// execute operation
		addOperation(new ShlOperation(getMode(node), Register._AX, constant));
		// store on stack
		storeValue(node, Register._AX);
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
	public void visit(Load node) {
		addOperation(new Comment("load operation " + node));
		Node referenceNode = node.getPred(1);
		getValue(referenceNode, Register._AX);
		addOperation(new MovOperation(Bit.BIT64, new StackPointer(0, Register._AX), Register._AX));
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				storeValue(edgeNode, Register._AX);
			}
		}
	}

	@Override
	public void visit(Store node) {
		addOperation(new Comment("Store operation " + node));
		Node addressNode = node.getPred(1);
		getValue(addressNode, Register._AX);
		Node valueNode = node.getPred(2);
		getValue(valueNode, Register._CX);
		addOperation(new MovOperation(getMode(valueNode), Register._CX, new StackPointer(0, Register._AX)));
	}

	@Override
	public void visit(Sub node) {
		// we subtract the right node from the left, not the otherway around
		visitTwoOperandsNode(new SubOperation("sub operation", getMode(node)), node, node.getRight(), node.getLeft());
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
			getValue(predecessor, Register._AX);
			StackPointer stackPointer = storeValueOnNewStackPointer(predecessor, Register._AX);
			phiTempStackMapping.put(phi, stackPointer);
		}

		for (Phi phi : phis) {
			getValue(phi, Register._AX, phiTempStackMapping.get(phi));
			storeValue(phi, Register._AX);
		}
	}
}
