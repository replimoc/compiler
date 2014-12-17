package compiler.firm.backend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import compiler.ast.CallingConvention;
import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.AndqOperation;
import compiler.firm.backend.operations.AssemblerOperation;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.MovlOperation;
import compiler.firm.backend.operations.MovqOperation;
import compiler.firm.backend.operations.PushqOperation;
import compiler.firm.backend.operations.RetOperation;
import compiler.firm.backend.operations.SizeOperation;
import compiler.utils.Utils;

import firm.Graph;
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

	private void operation(AssemblerOperation assemblerOption) {
		assembler.add(assemblerOption);
	}

	private void getValue(Node node, Register register) {
		operation(new Comment("get value"));
		// if variable was assigned, than simply load if from stack
		if (variableAssigned(node)) {
			operation(new MovlOperation(nodeStackOffsets.get(node), Register.RBP, register));
			// else we must collect all operations and save the result in register
		} else {

		}
	}

	private void storeValue(Node node, int value) {
		nodeStackOffsets.put(node, currentStackOffset);
		currentStackOffset -= 8; // 8 bytes per node
		operation(new MovlOperation(value, nodeStackOffsets.get(node), Register.RBP));
	}

	private void storeValue(Node node, Register value) {
		nodeStackOffsets.put(node, currentStackOffset);
		currentStackOffset -= 8; // 8 bytes per node
		operation(new MovlOperation(null, value, nodeStackOffsets.get(node), Register.RBP));
	}

	private boolean variableAssigned(Node node) {
		return nodeStackOffsets.containsKey(node);
	}

	@Override
	public void visit(Add node) {
		operation(new Comment("add operation"));

		// move left node to RAX
		getValue(node.getLeft(), Register.EAX);
		// move right node to RBX
		getValue(node.getRight(), Register.EDX);
		// add RAX to RBX
		operation(new AddOperation(Register.EAX, Register.EDX));
		// store on stack
		storeValue(node, Register.EDX);
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
		String methodName = graph.getEntity().getLdName();
		if (node.equals(graph.getStartBlock())) { // Start Block
			operation(new LabelOperation(methodName));
		} else if (node.equals(graph.getEndBlock())) {
			if (!Utils.isWindows()) {
				operation(new SizeOperation(methodName));
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
			Address callAddress = (Address) node.getPred(1);
			String methodName = callAddress.getEntity().getLdName();

			CallingConvention callingConvention = CallingConvention.SYSTEMV_ABI;
			if (callingConventions.containsKey(methodName)) {
				callingConvention = callingConventions.get(methodName);
			}
			switch (callingConvention) {
			case OWN:
				// TODO: Use our own calling convention
				// Until its implemented, use SYSTEMV_ABI.
				// break;
			case SYSTEMV_ABI:
				operation(new Comment(methodName));
				// Use System-V ABI calling convention
				operation(new Comment("save old stack pointer"));
				operation(new PushqOperation(Register.RSP, false));
				operation(new PushqOperation(Register.RSP, true));
				operation(new Comment("align stack to 16 bytes"));
				operation(new AndqOperation("-0x10", Register.RSP));
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
					Node parameter = node.getPred(i);
					// get value of parameter and save it in EAX
					getValue(parameter, Register.EAX);
					operation(new MovlOperation(Register.EAX, callingRegisters[i - 2]));
				}
				operation(new CallOperation(methodName));

				operation(new Comment("restore old stack pointer"));
				operation(new MovqOperation(Register.RSP, Register.RSP, 8));
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
		operation(new Comment("store const"));
		storeValue(node, node.getTarval().asInt());
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
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Raise node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Return node) {
		currentStackOffset = 0;
		operation(new Comment("restore stack size"));
		operation(new Debug("movq %rbp, %rsp"));
		operation(new Debug("popq %rbp"));
		operation(new RetOperation());
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
		operation(new Comment("allocate stack size"));
		operation(new Debug("pushq %rbp"));
		operation(new Debug("movq %rsp, %rbp"));
		// TODO: determine somehow how big the stack should be
		final int stackSize = 64; // 8 ints
		operation(new Debug("subq $" + stackSize + ", %rsp"));
	}

	@Override
	public void visit(Store node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sub node) {
		// TODO Auto-generated method stub

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
