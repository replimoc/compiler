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

	private final List<AssemblerOperation> operations = new ArrayList<>();
	private final HashMap<String, CallingConvention> callingConventions;
	private final HashMap<Block, LabelOperation> blockLabels = new HashMap<>();

	private Block currentBlock;
	private List<Phi> phis;

	private final RegisterAllocation registerAllocation;

	public X8664AssemblerGenerationVisitor(HashMap<String, CallingConvention> callingConventions) {
		this.callingConventions = callingConventions;
		this.registerAllocation = new RegisterAllocation(operations);
	}

	public List<AssemblerOperation> getOperations() {
		return operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	private <T extends StorageRegisterOperation> void visitTwoOperandsNode(T operation, Node parent, Node left, Node right) {
		// get left node
		Register registerLeft = registerAllocation.getValue(left, false);
		// get right node
		Register registerRight = registerAllocation.getValue(right, true);
		// TODO: find a nicer way to instantiate T directly instead of passing an instance and then initializing
		operation.initialize(registerLeft, registerRight);
		// execute operation
		addOperation(operation);
		// store on stack
		registerAllocation.storeValue(parent, registerRight);
		registerAllocation.freeRegister(registerLeft);
		registerAllocation.freeRegister(registerRight);
	}

	private LabelOperation getBlockLabel(Block node) {
		LabelOperation blockLabel = null;
		if (blockLabels.containsKey(node)) {
			blockLabel = blockLabels.get(node);
		} else {
			blockLabel = new LabelOperation("BLOCK_" + node.getNr());
		}
		return blockLabel;
	}

	public void addListOfAllPhis(List<Phi> phis) {
		this.phis = phis;
	}

	// ----------------------------------------------- NodeVisitor ---------------------------------------------------

	@Override
	public void visit(Add node) {
		visitTwoOperandsNode(new AddOperation("add operation", registerAllocation.getMode(node)), node, node.getLeft(), node.getRight());
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
			addOperation(new SubOperation("static stack reservation", Bit.BIT64, new Constant(RegisterAllocation.STACK_ITEM_SIZE * 64), Register._SP));

			registerAllocation.reserveMemoryForPhis(phis);
		}

		if (node.equals(graph.getEndBlock())) {
			addOperation(new AddOperation("free stack", Bit.BIT64, new Constant(RegisterAllocation.STACK_ITEM_SIZE * 64), Register._SP));
			registerAllocation.resetStackOffset();
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
			Constant parameterSize = new Constant(RegisterAllocation.STACK_ITEM_SIZE * remainingParameters);

			if (remainingParameters > 0) {
				addOperation(new SubOperation(Bit.BIT64, parameterSize, Register._SP));

				for (int i = 0; i < remainingParameters; i++) {
					Storage sourcePointer = registerAllocation.getStorage(node.getPred(i + firmOffset));
					// Copy parameter
					Register temporaryRegister = registerAllocation.getNewRegister();
					StackPointer destinationPointer = new StackPointer(i * RegisterAllocation.STACK_ITEM_SIZE, Register._SP);
					Bit mode = registerAllocation.getMode(node.getPred(i + firmOffset));
					addOperation(new MovOperation(mode, sourcePointer, temporaryRegister));
					addOperation(new MovOperation(mode, temporaryRegister, destinationPointer));
					registerAllocation.freeRegister(temporaryRegister);
				}
			}
			firmOffset -= callingRegisters.length;

			// TODO: Register Allocation: Possible save all conflict registers.

			for (int i = 0; i < parametersCount && i < callingRegisters.length; i++) {
				// Copy parameters in registers
				Node parameterNode = node.getPred(i + firmOffset);
				registerAllocation.getValue(parameterNode, true, callingRegisters[i]);
			}

			addOperation(new CallOperation(methodName));

			for (int i = 0; i < parametersCount && i < callingRegisters.length; i++) {
				registerAllocation.freeRegister(callingRegisters[i]);
			}

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
						registerAllocation.storeValue(innerEdge.node, callingConvention.getReturnRegister());
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
		registerAllocation.addConstant(node);
	}

	@Override
	public void visit(Conv node) {
		if (node.getPredCount() >= 1) {
			// TODO: This should maybe optimized with register allocation.
			Register register = registerAllocation.getValue(node.getPred(0), true);
			registerAllocation.storeValue(node, register);
			registerAllocation.freeRegister(register);
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
		Register reservedRegister = registerAllocation.getNewRegister(Register._DX); // Reserve modulo result register
		addOperation(new Comment("div operation"));
		// move left node to EAX
		Register registerLeft = registerAllocation.getValue(left, true, Register._AX);
		// move right node to RSI
		Register registerRight = registerAllocation.getValue(right, true, Register._SI); // TODO: Is this overwritten?
		addOperation(new CltdOperation());
		// idivl (eax / esi)
		addOperation(new DivOperation(registerAllocation.getMode(right), registerRight));
		// store on stack
		for (Edge edge : BackEdges.getOuts(node)) {
			registerAllocation.storeValue(edge.node, storeRegister);
		}
		registerAllocation.freeRegister(registerLeft);
		registerAllocation.freeRegister(registerRight);
		registerAllocation.freeRegister(reservedRegister);
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
		Register register = registerAllocation.getValue(node.getPred(0), true);
		addOperation(new NegOperation(registerAllocation.getMode(node), register));
		registerAllocation.storeValue(node, register);
		registerAllocation.freeRegister(register);
	}

	@Override
	public void visit(Mod node) {
		visitDivMod(node, node.getLeft(), node.getRight(), Register._DX);
	}

	@Override
	public void visit(Mul node) {
		visitTwoOperandsNode(new ImulOperation("mul operation", registerAllocation.getMode(node)), node, node.getRight(), node.getLeft());
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
		Register register1 = registerAllocation.getValue(node.getRight(), false);
		Register register2 = registerAllocation.getValue(node.getLeft(), false);
		addOperation(new CmpOperation("cmp operation", registerAllocation.getMode(node), register1, register2));
		registerAllocation.freeRegister(register1);
		registerAllocation.freeRegister(register2);
	}

	@Override
	public void visit(Proj node) {
		if (node.getPredCount() == 1 && node.getPred(0) instanceof Start && node.getMode().equals(Mode.getT())) {
			for (Edge edge : BackEdges.getOuts(node)) {
				if (edge.node instanceof Proj) {
					Proj proj = (Proj) edge.node;
					registerAllocation.addParamterToNodeStorage(proj);
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
			Register register = registerAllocation.getValue(node.getPred(1), true, Register._AX);
			registerAllocation.freeRegister(register);
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
		Register register = registerAllocation.getValue(node.getLeft(), true);

		Constant constant = new Constant((Const) node.getRight());

		// execute operation
		addOperation(new ShlOperation(registerAllocation.getMode(node), register, constant));
		// store on stack
		registerAllocation.storeValue(node, register);
		registerAllocation.freeRegister(register);
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
		Register register = registerAllocation.getValue(referenceNode, false);
		Register registerStore = registerAllocation.getNewRegister();
		addOperation(new MovOperation(Bit.BIT64, new StackPointer(0, register), registerStore));
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				registerAllocation.storeValue(edgeNode, registerStore);
			}
		}
		registerAllocation.freeRegister(register);
		registerAllocation.freeRegister(registerStore);
	}

	@Override
	public void visit(Store node) {
		addOperation(new Comment("Store operation " + node));
		Node addressNode = node.getPred(1);
		Register registerAddress = registerAllocation.getValue(addressNode, false);
		Node valueNode = node.getPred(2);
		Register registerOffset = registerAllocation.getValue(valueNode, false);
		addOperation(new MovOperation(registerAllocation.getMode(valueNode), registerOffset, new StackPointer(0, registerAddress)));
		registerAllocation.freeRegister(registerAddress);
		registerAllocation.freeRegister(registerOffset);
	}

	@Override
	public void visit(Sub node) {
		// we subtract the right node from the left, not the otherway around
		visitTwoOperandsNode(new SubOperation("sub operation", registerAllocation.getMode(node)), node, node.getRight(), node.getLeft());
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
			Register register = registerAllocation.getValue(predecessor, false);
			StackPointer stackPointer = registerAllocation.storeValueOnNewStackPointer(predecessor, register);
			registerAllocation.freeRegister(register);
			phiTempStackMapping.put(phi, stackPointer);
		}

		for (Phi phi : phis) {
			Register register = registerAllocation.getValue(phi, false, phiTempStackMapping.get(phi));
			registerAllocation.storeValue(phi, register);
			registerAllocation.freeRegister(register);
		}
	}
}
