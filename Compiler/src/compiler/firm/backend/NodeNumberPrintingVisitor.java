package compiler.firm.backend;

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

public class NodeNumberPrintingVisitor implements NodeVisitor {

	@Override
	public void visit(Add arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Address arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Align arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Alloc arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Anchor arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(And arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Bad arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Bitcast arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Block arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Builtin arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Call arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Cmp arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Cond arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Confirm arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Const arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Conv arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(CopyB arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Deleted arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Div arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Dummy arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(End arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Eor arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Free arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(IJmp arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Id arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Jmp arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Load arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Member arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Minus arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Mod arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Mul arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Mulh arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Mux arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(NoMem arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Not arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Offset arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Or arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Phi arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Pin arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Proj arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Raise arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Return arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Sel arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Shl arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Shr arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Shrs arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Size arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Start arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Store arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Sub arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Switch arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Sync arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Tuple arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visit(Unknown arg0) {
		System.out.println(arg0.getNr());
	}

	@Override
	public void visitUnknown(Node arg0) {
		System.out.println(arg0.getNr());
	}

}
