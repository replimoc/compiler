package compiler.firm.backend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.bit32.AddlOperation;
import compiler.firm.backend.operations.bit32.CmpOperation;
import compiler.firm.backend.operations.bit32.ImullOperation;
import compiler.firm.backend.operations.bit32.MovlOperation;
import compiler.firm.backend.operations.bit32.SublOperation;
import compiler.firm.backend.operations.bit32.TwoRegOperandsOperation;
import compiler.firm.backend.operations.bit64.AddqOperation;
import compiler.firm.backend.operations.bit64.CallOperation;
import compiler.firm.backend.operations.bit64.MovqOperation;
import compiler.firm.backend.operations.bit64.PopqOperation;
import compiler.firm.backend.operations.bit64.PushqOperation;
import compiler.firm.backend.operations.bit64.RetOperation;
import compiler.firm.backend.operations.bit64.SubqOperation;
import compiler.firm.backend.operations.general.Comment;
import compiler.firm.backend.operations.general.LabelOperation;
import compiler.firm.backend.operations.general.SizeOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
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
import firm.nodes.NodeVisitor;
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

public class X8664AssemblerGenerationVisitor implements NodeVisitor {

	private static final int STACK_ITEM_SIZE = 8;

	private HashMap<String, CallingConvention> callingConventions;
	private final List<AssemblerOperation> assembler = new LinkedList<AssemblerOperation>();
	private final HashMap<Node, Integer> nodeStackOffsets = new HashMap<>();
	private int currentStackOffset;

	public X8664AssemblerGenerationVisitor(HashMap<String, CallingConvention> callingConventions) {
		this.callingConventions = callingConventions;
	}

	public List<AssemblerOperation> getAssembler() {
		return assembler;
	}

	private void addOperation(AssemblerOperation assemblerOption) {
		assembler.add(assemblerOption);
	}

	private void getValue(Node node, Register register) {
		// if variable was assigned, than simply load if from stack
		if (variableAssigned(node)) {
			addOperation(new MovlOperation("Load node " + node.toString(),
					new StackPointer(getStackOffset(node), Register.RBP), register));
			// else we must collect all operations and save the result in register
		} else {

		}
	}

	private void getProjValue(Proj node, Register register) {
		// if variable was assigned, than simply load if from stack
		if (variableAssigned(node)) {
			addOperation(new MovqOperation("Load address " + node.toString(),
					new StackPointer(getStackOffset(node), Register.RBP), register));
			// else we must collect all operations and save the result in register
		} else {

		}
	}

	private int getStackOffset(Node node) {
		return nodeStackOffsets.get(node);
	}

	private void storeValue(Node node, Storage storage) {
		// Allocate stack
		currentStackOffset -= STACK_ITEM_SIZE;
		addOperation(new SubqOperation("Increment stack size", new Constant(STACK_ITEM_SIZE), Register.RSP));

		nodeStackOffsets.put(node, currentStackOffset);
		addOperation(new MovlOperation("Store node " + node,
				storage, new StackPointer(currentStackOffset, Register.RBP)));
	}

	private void storeProjValue(Proj node, Storage storage) {
		// Allocate stack
		currentStackOffset -= STACK_ITEM_SIZE;
		addOperation(new SubqOperation(new Constant(STACK_ITEM_SIZE), Register.RSP));

		nodeStackOffsets.put(node, currentStackOffset);
		addOperation(new MovqOperation("Store address " + node,
				storage, new StackPointer(currentStackOffset, Register.RBP)));
	}

	private boolean variableAssigned(Node node) {
		return nodeStackOffsets.containsKey(node);
	}

	private <T extends TwoRegOperandsOperation> void visitTwoOperandsNode(T operation, Node parent, Node left,
			Node right) {
		// move left node to RAX
		getValue(left, Register.EAX);
		// move right node to RBX
		getValue(right, Register.EDX);
		// TODO: find a nicer way to instantiate T directly instead of passing an instance and then initializing
		operation.initialize(Register.EAX, Register.EDX);
		// add RAX to RBX
		addOperation(operation);
		// store on stack
		storeValue(parent, Register.EDX);
	}

	@Override
	public void visit(Add node) {
		visitTwoOperandsNode(new AddlOperation("add operation"), node, node.getLeft(), node.getRight());
	}

	@Override
	public void visit(Address node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Align node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Alloc node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Anchor node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(And node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Bad node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Bitcast node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Block node) {
		Graph graph = node.getGraph();
		if (node.equals(graph.getEndBlock())) {
			String methodName = graph.getEntity().getLdName();
			if (!Utils.isWindows()) {
				addOperation(new SizeOperation(methodName));
			}
		}
	}

	@Override
	public void visit(Builtin node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Call node) {
		int predCount = node.getPredCount();
		if (predCount >= 2 && node.getPred(1) instanceof Address) { // Minimum for all calls
			int firmOffset = 2;
			int parametersCount = (predCount - firmOffset);
			Address callAddress = (Address) node.getPred(1);
			String methodName = callAddress.getEntity().getLdName();

			addOperation(new Comment("Call " + methodName));

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
				if (parameterNode instanceof Proj) {
					getProjValue((Proj) parameterNode, callingRegisters[i]);
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
						if (edge.node instanceof Proj) {
							storeProjValue((Proj) innerEdge.node, callingConvention.getReturnRegister());
						} else {
							storeValue(innerEdge.node, callingConvention.getReturnRegister());
						}
					}
				}
			}

		}

	}

	@Override
	public void visit(Cmp node) {
		visitTwoOperandsNode(new CmpOperation("cmp operation"), node, node.getLeft(), node.getRight());
	}

	@Override
	public void visit(Cond node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Confirm node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Const node) {
		addOperation(new Comment("store const"));
		storeValue(node, new Constant(node.getTarval().asInt()));
	}

	@Override
	public void visit(Conv node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CopyB node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Deleted node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Div node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Dummy node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(End node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Eor node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Free node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IJmp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Id node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Jmp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Load node) {
		Node referenceNode = node.getPred(1);
		if (referenceNode instanceof Proj) {
			getProjValue((Proj) referenceNode, Register.EAX);
		} else {
			getValue(referenceNode, Register.EAX);
		}
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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Minus node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Mod node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Mul node) {
		// we subtract the right node from the left, not the otherway around
		visitTwoOperandsNode(new ImullOperation("mul operation"), node, node.getRight(), node.getLeft());
	}

	@Override
	public void visit(Mulh node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Mux node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NoMem node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Not node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Offset node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Or node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Phi node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Pin node) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Return node) {
		addOperation(new Comment("restore stack size"));
		if (node.getPredCount() > 1) {
			// Store return value in EAX register
			getValue(node.getPred(1), Register.EAX);
		}

		addOperation(new AddqOperation(new Constant(-currentStackOffset), Register.RSP));
		addOperation(new PopqOperation(Register.RBP));
		addOperation(new RetOperation());
		currentStackOffset = 0;

	}

	@Override
	public void visit(Sel node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Shl node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Shr node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Shrs node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Size node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Start node) {
		Graph graph = node.getGraph();
		String methodName = graph.getEntity().getLdName();
		addOperation(new LabelOperation(methodName));

		addOperation(new PushqOperation(Register.RBP)); // Dynamic Link
		addOperation(new MovqOperation(Register.RSP, Register.RBP));
	}

	@Override
	public void visit(Store node) {
		Node source = node.getPred(1);
		if (source instanceof Proj) {
			getProjValue((Proj) source, Register.EAX);
		} else {
			getValue(source, Register.EAX);
		}
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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sync node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Tuple node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Unknown node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitUnknown(Node node) {
		// TODO Auto-generated method stub

	}

}
