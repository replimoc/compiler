package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;

import firm.Mode;
import firm.TargetValue;
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

public class OptimizationVisitor implements NodeVisitor {

	private final LinkedList<Node> workList;
	private final HashMap<Node, TargetValue> targets = new HashMap<>();
	// Div and Mod nodes have a Proj successor which must be replaced instead of the Div and Mod nodes themselves
	private final HashMap<Node, TargetValue> specialProjTargets = new HashMap<>();

	public OptimizationVisitor(LinkedList<Node> workList) {
		this.workList = workList;
	}

	public HashMap<Node, TargetValue> getTargetValues() {
		return targets;
	}

	private void workNode(Node node) {
		workList.offer(node);
	}

	private void workNodes(Iterable<Node> nodes) {
		for (Node node : nodes) {
			workList.offer(node);
		}
	}

	private void setTargetValue(Node node, TargetValue targetValue) {
		targets.put(node, targetValue);
	}

	private TargetValue getTarget(Node node) {
		return targets.get(node);
	}

	@Override
	public void visit(Add add) {
		TargetValue leftTarget = getTarget(add.getLeft());
		TargetValue rightTarget = getTarget(add.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.add(rightTarget);
			setTargetValue(add, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Address arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Align arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Alloc arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Anchor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(And and) {
		TargetValue leftTarget = getTarget(and.getLeft());
		TargetValue rightTarget = getTarget(and.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.and(rightTarget);
			setTargetValue(and, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Bad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Bitcast arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Block arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Builtin arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Call arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Cmp arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Cond arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Confirm arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Const constant) {
		if (constant.getMode().equals(Mode.getIs()) || constant.getMode().equals(Mode.getBu())) {
			setTargetValue(constant, constant.getTarval());
		}
	}

	@Override
	public void visit(Conv arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CopyB arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Deleted arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Div div) {
		// firm automatically skips exchanging the node if right target is null
		TargetValue leftTarget = getTarget(div.getLeft());
		TargetValue rightTarget = getTarget(div.getRight());

		if (leftTarget != null && rightTarget != null) {
			specialProjTargets.put(div, leftTarget.div(rightTarget));
		}

	}

	@Override
	public void visit(Dummy arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(End arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Eor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Free arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IJmp arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Id arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Jmp arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Load arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Member arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Minus minus) {
		TargetValue target = getTarget(minus.getOp());

		if (target != null) {
			TargetValue newTargetValue = target.neg();
			setTargetValue(minus, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Mod mod) {
		// firm automatically skips exchanging the node if right target is null
		TargetValue leftTarget = getTarget(mod.getLeft());
		TargetValue rightTarget = getTarget(mod.getRight());

		if (leftTarget != null && rightTarget != null) {
			specialProjTargets.put(mod, leftTarget.mod(rightTarget));
		}
	}

	@Override
	public void visit(Mul mul) {
		TargetValue leftTarget = getTarget(mul.getLeft());
		TargetValue rightTarget = getTarget(mul.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.mul(rightTarget);
			setTargetValue(mul, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Mulh arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Mux arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NoMem arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Not not) {
		TargetValue target = getTarget(not.getOp());

		if (target != null) {
			TargetValue newTargetValue = target.not();
			setTargetValue(not, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Offset arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Or or) {
		TargetValue leftTarget = getTarget(or.getLeft());
		TargetValue rightTarget = getTarget(or.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.or(rightTarget);
			setTargetValue(or, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Phi arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Pin arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Proj proj) {
		if (proj.getPredCount() == 1) {
			if (specialProjTargets.containsKey(proj.getPred(0))) {
				setTargetValue(proj, specialProjTargets.get(proj.getPred(0)));
			}
		}
	}

	@Override
	public void visit(Raise arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Return arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sel arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Shl shl) {
		TargetValue leftTarget = getTarget(shl.getLeft());
		TargetValue rightTarget = getTarget(shl.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.shr(rightTarget);
			setTargetValue(shl, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Shr shr) {
		TargetValue leftTarget = getTarget(shr.getLeft());
		TargetValue rightTarget = getTarget(shr.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.shr(rightTarget);
			setTargetValue(shr, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Shrs shrs) {
		TargetValue leftTarget = getTarget(shrs.getLeft());
		TargetValue rightTarget = getTarget(shrs.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.shr(rightTarget);
			setTargetValue(shrs, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Size arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Start arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Store arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sub sub) {
		TargetValue leftTarget = getTarget(sub.getLeft());
		TargetValue rightTarget = getTarget(sub.getRight());

		if (leftTarget != null && rightTarget != null) {
			TargetValue newTargetValue = leftTarget.sub(rightTarget, sub.getMode());
			setTargetValue(sub, newTargetValue);
			// TODO: add the nodes having this add as predecessor. How can we get them?
		}
	}

	@Override
	public void visit(Switch arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Sync arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Tuple arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Unknown arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitUnknown(Node arg0) {
		// TODO Auto-generated method stub

	}

}
