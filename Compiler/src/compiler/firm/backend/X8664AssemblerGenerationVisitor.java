package compiler.firm.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.FirmUtils;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.AndOperation;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.CltdOperation;
import compiler.firm.backend.operations.CmpOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.IdivOperation;
import compiler.firm.backend.operations.ImulOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.LeaOperation;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.NegOperation;
import compiler.firm.backend.operations.PopOperation;
import compiler.firm.backend.operations.PushOperation;
import compiler.firm.backend.operations.RetOperation;
import compiler.firm.backend.operations.ShlOperation;
import compiler.firm.backend.operations.SizeOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.dummy.FreeStackOperation;
import compiler.firm.backend.operations.dummy.ReserveStackOperation;
import compiler.firm.backend.operations.jump.JgOperation;
import compiler.firm.backend.operations.jump.JgeOperation;
import compiler.firm.backend.operations.jump.JlOperation;
import compiler.firm.backend.operations.jump.JleOperation;
import compiler.firm.backend.operations.jump.JmpOperation;
import compiler.firm.backend.operations.jump.JzOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.operations.templates.StorageRegisterOperationFactory;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
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

	private final ArrayList<AssemblerOperation> operations = new ArrayList<>();
	private final HashMap<String, CallingConvention> callingConventions;
	private final HashMap<Block, LabelOperation> blockLabels = new HashMap<>();

	private Block currentBlock;
	private List<Phi> phis;

	private final StorageManagement storageManagement;
	private final CallingConvention defaultCallingConvention = CallingConvention.SYSTEMV_ABI;

	public X8664AssemblerGenerationVisitor(HashMap<String, CallingConvention> callingConventions) {
		this.callingConventions = callingConventions;
		this.storageManagement = new StorageManagement(operations);
	}

	public ArrayList<AssemblerOperation> getOperations() {
		return operations;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		operations.add(assemblerOption);
	}

	private <T extends StorageRegisterOperation> void visitTwoOperandsNode(StorageRegisterOperationFactory operationFactory, Node parent,
			Node left, Node right) {
		// get left node
		Storage registerLeft = storageManagement.getValueAvoidNewRegister(left);
		// get right node
		RegisterBased registerRight = storageManagement.getValue(right, true);
		// create operation object
		StorageRegisterOperation operation = operationFactory.instantiate(registerLeft, registerRight);
		// execute operation
		addOperation(operation);
		// store on stack
		storageManagement.storeValue(parent, registerRight);
	}

	private LabelOperation getBlockLabel(Block node) {
		LabelOperation blockLabel = null;
		if (blockLabels.containsKey(node)) {
			blockLabel = blockLabels.get(node);
		} else {
			blockLabel = new LabelOperation("BLOCK_" + node.getNr());
			blockLabels.put(node, blockLabel);
		}
		return blockLabel;
	}

	public void addListOfAllPhis(List<Phi> phis) {
		this.phis = phis;
	}

	// ----------------------------------------------- NodeVisitor ---------------------------------------------------

	private boolean leaIsPossible(Add node) {
		if (node.getMode().equals(FirmUtils.getModeReference()) && node.getPred(1).getClass() == Shl.class) {
			Shl shift = (Shl) node.getPred(1);
			if (leaIsPossible(shift)) {
				return true;
			}
		}
		return false;
	}

	private boolean leaIsPossible(Shl shift) {
		return BackEdges.getNOuts(shift) == 1
				&& FirmUtils.getFirstSuccessor(shift).getClass() == Add.class
				&& FirmUtils.getFirstSuccessor(shift).getMode().equals(FirmUtils.getModeReference())
				&& leaFactor(shift) >= 0;
	}

	private int leaFactor(Shl shift) {
		if (shift.getPred(1).getClass() == Const.class) {
			Const constant = (Const) shift.getPred(1);
			int factor = (int) Math.pow(2, constant.getTarval().asInt());
			if (factor == 1 || factor == 2 || factor == 4 || factor == 8) {
				return factor;
			}
		}
		return -1;
	}

	private MemoryPointer calculateMemoryPointer(Add add) {
		Shl shift = (Shl) add.getPred(1);
		RegisterBased baseRegister = storageManagement.getValue(add.getPred(0), false);
		RegisterBased factorRegister = storageManagement.getValue(shift.getPred(0), false);
		int factor = leaFactor(shift);
		return new MemoryPointer(0, baseRegister, factorRegister, factor);
	}

	@Override
	public void visit(Add node) {
		if (leaIsPossible(node)) {
			if (BackEdges.getNOuts(node) == 1 && (
					FirmUtils.getFirstSuccessor(node).getClass() == Store.class ||
					FirmUtils.getFirstSuccessor(node).getClass() == Load.class
					)) {
				return;
			}
			Storage address = calculateMemoryPointer(node);

			VirtualRegister resultRegister = new VirtualRegister(Bit.BIT64);
			addOperation(new LeaOperation(address, resultRegister));
			storageManagement.addStorage(node, resultRegister);
			return;
		}
		visitTwoOperandsNode(AddOperation.getFactory("add operation"), node, node.getLeft(), node.getRight());
	}

	@Override
	public void visit(Block node) {
		currentBlock = node;

		Graph graph = node.getGraph();
		String methodName = getMethodName(node);

		// we are in start block: initialize stack (RBP)
		if (node.equals(graph.getStartBlock())) {
			addOperation(new LabelOperation(methodName));
			methodStart(node);
		}

		if (node.equals(graph.getEndBlock())) {
			if (!Utils.isWindows()) {
				addOperation(new SizeOperation(methodName));
			}
		}

		// prepend a label before each block
		addOperation(getBlockLabel(node));
	}

	@Override
	public void visit(Call node) {
		int predCount = node.getPredCount();
		assert predCount >= 2 && node.getPred(1) instanceof Address : "Minimum for all calls";

		String methodName = ((Address) node.getPred(1)).getEntity().getLdName();

		List<CallOperation.Parameter> parameters = new LinkedList<>();
		for (int i = 2; i < predCount; i++) {
			Node parameter = node.getPred(i);
			parameters.add(new CallOperation.Parameter(storageManagement.getStorage(parameter), StorageManagement.getMode(parameter)));
		}

		CallingConvention callingConvention = getCallingConvention(methodName);

		addOperation(new CallOperation(methodName, parameters, callingConvention));

		for (Edge edge : BackEdges.getOuts(node)) {
			if (edge.node.getMode().equals(Mode.getT())) {
				for (Edge innerEdge : BackEdges.getOuts(edge.node)) {
					Node innerNode = innerEdge.node;
					Bit mode = StorageManagement.getMode(innerNode);
					storageManagement.storeValueAndCreateNewStorage(innerNode, callingConvention.getReturnRegister().getRegister(mode), true);
				}
			}
		}
	}

	@Override
	public void visit(Cond node) {
		Cmp cmpNode = (Cmp) node.getPred(0);
		Block blockTrue = null;
		Block blockFalse = null;

		// get blocks the cond node shows to
		Iterator<Edge> outs = BackEdges.getOuts(node).iterator();
		Proj out1 = (Proj) outs.next().node;
		Proj out2 = (Proj) outs.next().node;
		Block block1 = ((Block) (BackEdges.getOuts(out1).iterator().next().node));
		Block block2 = ((Block) (BackEdges.getOuts(out2).iterator().next().node));

		if (out1.getNum() == FirmUtils.TRUE) {
			blockTrue = block1;
			blockFalse = block2;
		} else {
			blockTrue = block2;
			blockFalse = block1;
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

	private void visitCmpNode(Cmp node) {
		Storage register1 = storageManagement.getValueAvoidNewRegister(node.getRight());
		RegisterBased register2 = storageManagement.getValue(node.getLeft(), false);
		addOperation(new CmpOperation("cmp operation", register1, register2));
	}

	@Override
	public void visit(Const node) {
		// nothing to do
		storageManagement.addConstant(node);
	}

	@Override
	public void visit(Conv node) {
		assert node.getPredCount() >= 1 : "Conv nodes must have a predecessor";

		Storage register = storageManagement.getValueAvoidNewRegister(node.getPred(0));
		storageManagement.addStorage(node, register);
	}

	private void visitDivMod(Node node, Node left, Node right, SingleRegister storeRegister) {
		addOperation(new Comment("div operation"));
		// move left node to EAX
		storageManagement.getValue(left, RegisterBundle._AX);
		// move right node to RSI
		RegisterBased registerRight = storageManagement.getValue(right, false);
		addOperation(new CltdOperation());
		// idivl (eax / esi)
		addOperation(new IdivOperation(registerRight));
		// store on stack
		for (Edge edge : BackEdges.getOuts(node)) {
			storageManagement.storeValueAndCreateNewStorage(edge.node, storeRegister, true);
		}
	}

	@Override
	public void visit(Div node) {
		visitDivMod(node, node.getLeft(), node.getRight(), SingleRegister.EAX);
	}

	@Override
	public void visit(End node) {
		addOperation(new Comment("end node"));
	}

	@Override
	public void visit(Jmp node) {
		addOperation(new JmpOperation(getBlockLabel((Block) FirmUtils.getFirstSuccessor(node))));
	}

	@Override
	public void visit(Minus node) {
		RegisterBased register = storageManagement.getValue(node.getPred(0), true);
		addOperation(new NegOperation(register));
		storageManagement.storeValue(node, register);
	}

	@Override
	public void visit(Mod node) {
		visitDivMod(node, node.getLeft(), node.getRight(), SingleRegister.EDX);
	}

	@Override
	public void visit(Mul node) {
		visitTwoOperandsNode(ImulOperation.getFactory("mul operation"), node, node.getRight(), node.getLeft());
	}

	@Override
	public void visit(Return node) {
		if (node.getPredCount() > 1) {
			// Store return value in EAX register
			storageManagement.getValue(node.getPred(1), RegisterBundle._AX);
		}
		methodEnd(node);
		addOperation(new RetOperation(getMethodName(node)));
	}

	@Override
	public void visit(Shl node) {
		if (leaIsPossible(node)) {
			return; // This case is handled in visit(Add)
		}
		// move left node to a register
		RegisterBased register = storageManagement.getValue(node.getLeft(), true);

		Constant constant = new Constant((Const) node.getRight());

		// execute operation
		addOperation(new ShlOperation(StorageManagement.getMode(node), register, constant));
		// store on stack
		storageManagement.storeValue(node, register);
	}

	public MemoryPointer getMemoryPointerForNode(Node addressNode) {
		MemoryPointer memory = null;
		if (addressNode.getClass() == Add.class && leaIsPossible((Add) addressNode) && BackEdges.getNOuts(addressNode) == 1) {
			memory = calculateMemoryPointer((Add) addressNode);
		} else {
			RegisterBased registerAddress = storageManagement.getValue(addressNode, false);
			memory = new MemoryPointer(0, registerAddress);
		}
		return memory;
	}

	@Override
	public void visit(Load node) {
		addOperation(new Comment("load operation " + node));
		MemoryPointer memory = getMemoryPointerForNode(node.getPred(1));
		VirtualRegister registerStore = new VirtualRegister(StorageManagement.getMode(node.getLoadMode()));
		addOperation(new MovOperation(memory, registerStore));
		for (Edge edge : BackEdges.getOuts(node)) {
			Node edgeNode = edge.node;
			if (!edgeNode.getMode().equals(Mode.getM())) {
				storageManagement.storeValue(edgeNode, registerStore);
			}
		}
	}

	@Override
	public void visit(Store node) {
		addOperation(new Comment("Store operation " + node));
		MemoryPointer memory = getMemoryPointerForNode(node.getPred(1));
		Node valueNode = node.getPred(2);
		RegisterBased registerOffset = storageManagement.getValue(valueNode, false);
		addOperation(new MovOperation(registerOffset, memory));
	}

	@Override
	public void visit(Sub node) { // we subtract the right node from the left, not the otherway around
		visitTwoOperandsNode(SubOperation.getFactory("sub operation"), node, node.getRight(), node.getLeft());
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

	private CallingConvention getCallingConvention(String methodName) {
		CallingConvention callingConvention = this.defaultCallingConvention;
		if (callingConventions.containsKey(methodName)) {
			callingConvention = callingConventions.get(methodName);
		}
		return callingConvention;
	}

	private String getMethodName(Node node) {
		return node.getGraph().getEntity().getLdName();
	}

	private void methodStart(Node node) {
		addOperation(new PushOperation(Bit.BIT64, SingleRegister.RBP)); // Dynamic Link
		addOperation(new MovOperation(SingleRegister.RSP, SingleRegister.RBP));

		CallingConvention callingConvention = getCallingConvention(getMethodName(node));
		for (RegisterBundle register : callingConvention.calleeSavedRegisters()) {
			addOperation(new PushOperation(Bit.BIT64, register.getRegister(Bit.BIT64)));
		}

		addOperation(new ReserveStackOperation());

		Node args = node.getGraph().getArgs();
		RegisterBundle[] parameterRegisters = callingConvention.getParameterRegisters();
		for (Edge edge : BackEdges.getOuts(args)) {
			if (edge.node instanceof Proj) {
				Proj proj = (Proj) edge.node;
				Bit mode = StorageManagement.getMode(proj);
				Storage location = new VirtualRegister(mode);

				if (proj.getNum() < parameterRegisters.length) {
					RegisterBundle registerBundle = parameterRegisters[proj.getNum()];
					VirtualRegister storage = new VirtualRegister(mode, registerBundle);
					if (registerBundle.getRegister(mode) == null) {
						addOperation(new MovOperation(storage, location));
						// TODO: the mask should be saved in the mode
						addOperation(new AndOperation(new Constant(0xFF), (VirtualRegister) location));
					} else {
						addOperation(new MovOperation(storage, location));
					}
				} else {
					// TODO: Do this only, if more than one usage is available
					// + 2 for dynamic link
					MemoryPointer storage = new MemoryPointer(STACK_ITEM_SIZE * (proj.getNum() + 2 - parameterRegisters.length), SingleRegister.RBP);
					addOperation(new MovOperation(storage, location));
				}
				storageManagement.addStorage(proj, location);
			}
		}

		storageManagement.reserveMemoryForPhis(phis);
	}

	private void methodEnd(Node node) {
		addOperation(new FreeStackOperation());

		CallingConvention callingConvention = getCallingConvention(getMethodName(node));

		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		for (int i = registers.length - 1; i >= 0; i--) {
			addOperation(new PopOperation(registers[i].getRegister(Bit.BIT64)));
		}

		addOperation(new MovOperation(SingleRegister.RBP, SingleRegister.RSP));
		addOperation(new PopOperation(SingleRegister.RBP));
	}

	@Override
	public void visit(List<Phi> phis) {
		addOperation(new Comment("Handle phis of current block"));

		HashMap<Node, Phi> node2phiMapping = new HashMap<>();
		List<Phi> conflictNodes = new ArrayList<>();

		for (Phi phi : phis) {
			Node predecessor = getRelevantPredecessor(phi);

			if (phis.contains(predecessor)) {
				conflictNodes.add(phi);
			} else {
				node2phiMapping.put(predecessor, phi);
			}
		}

		HashMap<Phi, Storage> phiTempStackMapping = new HashMap<>();
		for (Phi phi : conflictNodes) {
			Node predecessor = getRelevantPredecessor(phi);
			Storage register = storageManagement.getValueAvoidNewRegister(predecessor);
			Storage temporaryStorage = new VirtualRegister(StorageManagement.getMode(predecessor));
			storageManagement.storeValue(predecessor, register, temporaryStorage);
			phiTempStackMapping.put(phi, temporaryStorage);
		}

		for (Entry<Node, Phi> mapping : node2phiMapping.entrySet()) {
			Storage register = storageManagement.getValueAvoidNewRegister(mapping.getKey());
			Storage destination = storageManagement.getStorage(mapping.getValue());
			addOperation(new MovOperation("Phi: " + mapping.getKey() + " -> " + mapping.getValue(), register, destination));
		}

		for (Phi phi : conflictNodes) {
			storageManagement.storeValue(phi, phiTempStackMapping.get(phi));
		}
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
	public void visit(Raise node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Sel node) {
		throw new RuntimeException(node + " is not implemented yet!");
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
	public void visit(Bitcast node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Confirm node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(CopyB node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Deleted node) {
		throw new RuntimeException(node + " is not implemented yet!");
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

	@Override
	public void visit(Proj node) {
	}

	@Override
	public void visit(Dummy node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Eor node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Builtin node) {
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
	public void visit(Member node) {
		throw new RuntimeException(node + " is not implemented yet!");
	}

	@Override
	public void visit(Address node) {
		// This is handled in a call.
	}

	@Override
	public void visit(Bad node) {
		// Ignore Bad nodes.
	}

	@Override
	public void visit(Cmp node) {
		// Nothing to do here, its handled in Cond.
	}

	@Override
	public void visit(Size node) {
	}

	@Override
	public void visit(Start node) {
	}

}
