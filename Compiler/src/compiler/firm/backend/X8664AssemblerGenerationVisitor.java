package compiler.firm.backend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import compiler.ast.CallingConvention;
import compiler.firm.backend.operations.bit32.AddlOperation;
import compiler.firm.backend.operations.bit32.MovlOperation;
import compiler.firm.backend.operations.bit32.SublOperation;
import compiler.firm.backend.operations.bit32.TwoRegOperandsOperation;
import compiler.firm.backend.operations.bit64.AddqOperation;
import compiler.firm.backend.operations.bit64.AndqOperation;
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
		addOperation(new Comment("get value"));
		// if variable was assigned, than simply load if from stack
		if (variableAssigned(node)) {
			addOperation(new MovlOperation(new StackPointer(getStackOffset(node), Register.RBP), register));
			// else we must collect all operations and save the result in register
		} else {

		}
	}

	private int getStackOffset(Node node) {
		return nodeStackOffsets.get(node);
	}

	private void storeValue(Node node, Storage storage) {
		// Allocate stack
		addOperation(new SubqOperation(new Constant(STACK_ITEM_SIZE), Register.RSP));

		nodeStackOffsets.put(node, currentStackOffset);
		currentStackOffset -= STACK_ITEM_SIZE;
		addOperation(new MovlOperation(storage, new StackPointer(getStackOffset(node), Register.RBP)));
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
		addOperation(new Comment("add operation"));

		visitTwoOperandsNode(new AddlOperation(), node, node.getLeft(), node.getRight());
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
			int parametersCount = (predCount - 2);
			Address callAddress = (Address) node.getPred(1);
			String methodName = callAddress.getEntity().getLdName();

			CallingConvention callingConvention = CallingConvention.SYSTEMV_ABI;
			if (callingConventions.containsKey(methodName)) {
				callingConvention = callingConventions.get(methodName);
			}
			switch (callingConvention) {
			case OWN:
				Constant parameterSize = new Constant(STACK_ITEM_SIZE * (parametersCount + 1));
				addOperation(new SubqOperation(parameterSize, Register.RSP));

				// TODO: Address of object

				for (int i = 1; i < parametersCount; i++) {
					int parameterOffset = getStackOffset(node.getPred(i + 2));
					// Copy parameter
					addOperation(new MovlOperation(new StackPointer(parameterOffset, Register.RBP), Register.EAX));
					addOperation(new MovlOperation(Register.EAX, new StackPointer((i + 1) * STACK_ITEM_SIZE, Register.RSP)));
				}
				addOperation(new CallOperation(methodName));

				// TODO: Save return parameter

				addOperation(new AddqOperation(parameterSize, Register.RSP));
				break;
			case SYSTEMV_ABI:
				addOperation(new Comment(methodName));
				// Use System-V ABI calling convention
				addOperation(new Comment("save old stack pointer"));
				addOperation(new PushqOperation(Register.RSP));
				addOperation(new PushqOperation(new StackPointer(0, Register.RSP)));
				addOperation(new Comment("align stack to 16 bytes"));
				addOperation(new AndqOperation(new Constant(-0x10), Register.RSP));
				Register[] callingRegisters = { Register.EDI, Register.ESI, Register.EDX, Register.ECX };
				/*
				 * DON'T REMOVE ME. IT'S A PROBLEM OF OUR CODE, THAT SHOULD BE CONSIDERED
				 * 
				 * Does it make sense handling Proj nodes? I think we should ignore them and start from index 3
				 * 
				 * If we start iterating from index 3, than the first parameter should be passed in ESI register. (print_int expects it at least)
				 * 
				 * Currently the order EDI, ESI, ... seems to be wrong at least for print_int.
				 */
				for (int i = 2; i < predCount && (i - 2) < callingRegisters.length; i++) {
					// Copy parameters in registers for System-V calling convention
					getValue(node.getPred(i), callingRegisters[i - 2]);
				}
				addOperation(new CallOperation(methodName));

				addOperation(new Comment("restore old stack pointer"));
				addOperation(new MovqOperation(new StackPointer(8, Register.RSP), Register.RSP));

				// TODO: Save return parameter
				break;
			}
		}

	}

	@Override
	public void visit(Cmp node) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
			int stackPointerReference = STACK_ITEM_SIZE * 2; // Dynamic Link, Return Value
			for (Edge edge : BackEdges.getOuts(node)) {
				stackPointerReference += STACK_ITEM_SIZE;
				nodeStackOffsets.put(edge.node, stackPointerReference);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sub node) {
		addOperation(new Comment("sub operation"));

		visitTwoOperandsNode(new SublOperation(), node, node.getLeft(), node.getRight());
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
