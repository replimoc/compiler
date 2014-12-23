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
import firm.Relation;
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
	private final HashMap<Node, Integer> nodeStackOffsets = new HashMap<>();
	private int currentStackOffset;
	private Block currentBlock;

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

		// if variable was assigned, than simply load if from stack
		if (variableAssigned(node)) {

			StackPointer stackPointer = new StackPointer(getStackOffset(node), Register.RBP);
			getValue(node, register, stackPointer);
		} else { // else we must collect all operations and save the result in register

		}
	}

	private void getValue(Node node, Register register, StackPointer stackPointer) {
		if (is64bitNode(node)) {
			addOperation(new MovqOperation("Load address " + node.toString(), stackPointer, register));
		} else {
			addOperation(new MovlOperation("Load node " + node.toString(), stackPointer, register));
		}
	}

	private int getStackOffset(Node node) {
		return nodeStackOffsets.get(node);
	}

	private StackPointer storeValueAndGetStackPointer(Node node, Storage storage) {
		// Allocate stack
		currentStackOffset -= STACK_ITEM_SIZE;
		addOperation(new SubqOperation("Increment stack size", new Constant(STACK_ITEM_SIZE), Register.RSP));

		StackPointer stackPointer = new StackPointer(currentStackOffset, Register.RBP);
		storeValue(node, storage, stackPointer);
		return stackPointer;
	}

	private void storeValue(Node node, Storage storage) {
		StackPointer stackPointer = storeValueAndGetStackPointer(node, storage);
		nodeStackOffsets.put(node, stackPointer.getOffset());
	}

	private void storeValue(Node node, Storage storage, StackPointer stackPointer) {
		if (is64bitNode(node)) {
			addOperation(new MovqOperation("Store node " + node, storage, stackPointer));
		} else {
			addOperation(new MovlOperation("Store node " + node, storage, stackPointer));
		}
	}

	private boolean variableAssigned(Node node) {
		return nodeStackOffsets.containsKey(node);
	}

	private <T extends StorageRegisterOperation> void visitTwoOperandsNode(T operation, Node parent, Node left,
			Node right) {
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
		throw new RuntimeException(node + " is not implemented yet!");
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
					int parameterOffset = getStackOffset(node.getPred(i + firmOffset));
					// Copy parameter
					StackPointer sourcePointer = new StackPointer(parameterOffset, Register.RBP);
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

				// because constNode returns incorrect block, perform "optimization" and load constant
				// into register directly see issue #202
				// TODO: Investigate why this happens and probably remove this "optimiziation" -> move it to getValue
				if (parameterNode instanceof Const) {
					Const constNode = (Const) parameterNode;
					addOperation(new MovlOperation(new Constant(constNode), callingRegisters[i]));
				} else {
					getValue(parameterNode, callingRegisters[i]);
				}
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

		// now add conditional jump
		if (cmpNode.getRelation() == Relation.Equal) {
			addOperation(CondJumpOperation.createJumpZero(getBlockLabel(blockTrue)));
			addOperation(CondJumpOperation.createJump(getBlockLabel(blockFalse)));
		} else if (cmpNode.getRelation() == Relation.Less) {
			addOperation(CondJumpOperation.createJumpLess(getBlockLabel(blockTrue)));
			addOperation(CondJumpOperation.createJump(getBlockLabel(blockFalse)));
		} else if (cmpNode.getRelation() == Relation.LessEqual) {
			addOperation(CondJumpOperation.createJumpLessEqual(getBlockLabel(blockTrue)));
			addOperation(CondJumpOperation.createJump(getBlockLabel(blockFalse)));
		} else if (cmpNode.getRelation() == Relation.Greater) {
			addOperation(CondJumpOperation.createJumpGreater(getBlockLabel(blockTrue)));
			addOperation(CondJumpOperation.createJump(getBlockLabel(blockFalse)));
		} else if (cmpNode.getRelation() == Relation.GreaterEqual) {
			addOperation(CondJumpOperation.createJumpGreaterEqual(getBlockLabel(blockTrue)));
			addOperation(CondJumpOperation.createJump(getBlockLabel(blockFalse)));
		}
	}

	@Override
	public void visit(Confirm node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Const node) {
		addOperation(new Comment("store const"));
		storeValue(node, new Constant(node));
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

	public void visitDivMod(Node node, Node left, Node right, Register storeRegister) {
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
		throw new RuntimeException(node + " is not implemented yet!");
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
					nodeStackOffsets.put(proj, STACK_ITEM_SIZE * (proj.getNum() + 2)); // + 2 for dynamic link
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
		System.out.println(node.getLeft());

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
		HashMap<Phi, StackPointer> phiStackMapping = new HashMap<>();
		for (Phi phi : phis) {
			Node predecessor = getRelevantPredecessor(phi);
			getValue(predecessor, Register.EAX);
			StackPointer stackPointer = storeValueAndGetStackPointer(predecessor, Register.EAX);
			phiStackMapping.put(phi, stackPointer);
		}

		for (Phi phi : phis) {
			StackPointer stackPointer = phiStackMapping.get(phi);
			getValue(phi, Register.EAX, stackPointer);

			if (variableAssigned(phi)) {
				// There is already a space on the stack, use it to save it
				storeValue(phi, Register.EAX, new StackPointer(getStackOffset(phi), Register.RBP));
			} else {
				// Create new stack place
				storeValue(phi, Register.EAX);
			}
		}
	}
}
