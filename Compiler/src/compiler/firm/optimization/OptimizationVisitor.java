package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

	// TODO: remove if possible!
	private final HashMap<Node, Iterable<Node>> tempSuccessor = new HashMap<>();

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
		if (nodes == null)
			return;
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

	// TODO: remove if possible!
	private void addSuccessor(Node node, Iterable<Node> nodes) {
		for (Node predNode : nodes) {
			Iterable<Node> pred = tempSuccessor.get(predNode);
			List<Node> predNodes = new LinkedList<Node>();
			if (pred != null) {
				for (Node n : pred) {
					predNodes.add(n);
				}
			}
			if (!predNodes.contains(node))
				predNodes.add(node);
			tempSuccessor.put(predNode, predNodes);
		}
	}

	@Override
	public void visit(Add add) {
		addSuccessor(add, add.getPreds());
		TargetValue target = getTarget(add);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(add.getLeft()) == null ? TargetValue.getUnknown() : getTarget(add.getLeft());
		TargetValue rightTarget = getTarget(add.getRight()) == null ? TargetValue.getUnknown() : getTarget(add.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(add, leftTarget.add(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(add, TargetValue.getBad());
		} else {
			setTargetValue(add, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(add))) {
			workNodes(tempSuccessor.get(add));
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
		addSuccessor(and, and.getPreds());
		TargetValue target = getTarget(and);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(and.getLeft()) == null ? TargetValue.getUnknown() : getTarget(and.getLeft());
		TargetValue rightTarget = getTarget(and.getRight()) == null ? TargetValue.getUnknown() : getTarget(and.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(and, leftTarget.and(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(and, TargetValue.getBad());
		} else {
			setTargetValue(and, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(and))) {
			workNodes(tempSuccessor.get(and));
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
		// // firm automatically skips exchanging the node if right target is null
		addSuccessor(div, div.getPreds());
		TargetValue target = getTarget(div);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(div.getLeft()) == null ? TargetValue.getUnknown() : getTarget(div.getLeft());
		TargetValue rightTarget = getTarget(div.getRight()) == null ? TargetValue.getUnknown() : getTarget(div.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			specialProjTargets.put(div, leftTarget.div(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			specialProjTargets.put(div, TargetValue.getBad());
		} else {
			specialProjTargets.put(div, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(div))) {
			workNodes(tempSuccessor.get(div));
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
		// TargetValue target = getTarget(minus.getOp());
		//
		// if (target != null) {
		// TargetValue newTargetValue = target.neg();
		// setTargetValue(minus, newTargetValue);
		// // TODO: add the nodes having this add as predecessor. How can we get them?
		// }
	}

	@Override
	public void visit(Mod mod) {
		// // firm automatically skips exchanging the node if right target is null
		addSuccessor(mod, mod.getPreds());
		TargetValue target = getTarget(mod);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(mod.getLeft()) == null ? TargetValue.getUnknown() : getTarget(mod.getLeft());
		TargetValue rightTarget = getTarget(mod.getRight()) == null ? TargetValue.getUnknown() : getTarget(mod.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			specialProjTargets.put(mod, leftTarget.mod(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			specialProjTargets.put(mod, TargetValue.getBad());
		} else {
			specialProjTargets.put(mod, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(mod))) {
			workNodes(tempSuccessor.get(mod));
		}
	}

	@Override
	public void visit(Mul mul) {
		addSuccessor(mul, mul.getPreds());
		TargetValue target = getTarget(mul);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(mul.getLeft()) == null ? TargetValue.getUnknown() : getTarget(mul.getLeft());
		TargetValue rightTarget = getTarget(mul.getRight()) == null ? TargetValue.getUnknown() : getTarget(mul.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(mul, leftTarget.mul(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(mul, TargetValue.getBad());
		} else {
			setTargetValue(mul, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(mul))) {
			workNodes(tempSuccessor.get(mul));
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
		// TargetValue target = getTarget(not.getOp());
		//
		// if (target != null) {
		// TargetValue newTargetValue = target.not();
		// setTargetValue(not, newTargetValue);
		// // TODO: add the nodes having this add as predecessor. How can we get them?
		// }
	}

	@Override
	public void visit(Offset arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Or or) {
		addSuccessor(or, or.getPreds());
		TargetValue target = getTarget(or);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(or.getLeft()) == null ? TargetValue.getUnknown() : getTarget(or.getLeft());
		TargetValue rightTarget = getTarget(or.getRight()) == null ? TargetValue.getUnknown() : getTarget(or.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(or, leftTarget.or(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(or, TargetValue.getBad());
		} else {
			setTargetValue(or, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(or))) {
			workNodes(tempSuccessor.get(or));
		}
	}

	@Override
	public void visit(Phi phi) {
		addSuccessor(phi, phi.getPreds());
		TargetValue target = getTarget(phi);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue predTarget = getTarget(phi.getPred(0)) == null ? TargetValue.getUnknown() : getTarget(phi.getPred(0));
		for (int i = 1; i < phi.getPredCount(); i++) {
			TargetValue tmpTarget = getTarget(phi.getPred(i)) == null ? TargetValue.getUnknown() : getTarget(phi.getPred(i));

			if (predTarget.isConstant() && tmpTarget.isConstant() && predTarget.equals(tmpTarget)) {
				setTargetValue(phi, predTarget);
			} else if (predTarget.equals(TargetValue.getBad()) || tmpTarget.equals(TargetValue.getBad())
					|| (predTarget.isConstant() && tmpTarget.isConstant() && !predTarget.equals(tmpTarget))) {
				setTargetValue(phi, TargetValue.getBad());
				break;
			} else if (tmpTarget.equals(TargetValue.getUnknown())) {
				setTargetValue(phi, predTarget);
			} else {
				setTargetValue(phi, tmpTarget);
				predTarget = tmpTarget;
			}
		}

		if (target == null || !target.equals(getTarget(phi))) {
			workNodes(tempSuccessor.get(phi));
		}
	}

	@Override
	public void visit(Pin arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Proj proj) {
		addSuccessor(proj, proj.getPreds());
		TargetValue target = getTarget(proj);
		if (proj.getPredCount() == 1) {
			if (specialProjTargets.containsKey(proj.getPred(0))) {
				TargetValue tar = specialProjTargets.get(proj.getPred(0));
				if (tar != null) {
					setTargetValue(proj, tar);
				}
			} else {
				setTargetValue(proj, TargetValue.getBad());
			}
		} else {
			setTargetValue(proj, TargetValue.getBad());
		}

		if (target == null || !target.equals(getTarget(proj))) {
			workNodes(tempSuccessor.get(proj));
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
		addSuccessor(shl, shl.getPreds());
		TargetValue target = getTarget(shl);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(shl.getLeft()) == null ? TargetValue.getUnknown() : getTarget(shl.getLeft());
		TargetValue rightTarget = getTarget(shl.getRight()) == null ? TargetValue.getUnknown() : getTarget(shl.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shl, leftTarget.shl(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shl, TargetValue.getBad());
		} else {
			setTargetValue(shl, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(shl))) {
			workNodes(tempSuccessor.get(shl));
		}
	}

	@Override
	public void visit(Shr shr) {
		addSuccessor(shr, shr.getPreds());
		TargetValue target = getTarget(shr);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(shr.getLeft()) == null ? TargetValue.getUnknown() : getTarget(shr.getLeft());
		TargetValue rightTarget = getTarget(shr.getRight()) == null ? TargetValue.getUnknown() : getTarget(shr.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shr, leftTarget.shr(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shr, TargetValue.getBad());
		} else {
			setTargetValue(shr, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(shr))) {
			workNodes(tempSuccessor.get(shr));
		}
	}

	@Override
	public void visit(Shrs shrs) {
		addSuccessor(shrs, shrs.getPreds());
		TargetValue target = getTarget(shrs);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(shrs.getLeft()) == null ? TargetValue.getUnknown() : getTarget(shrs.getLeft());
		TargetValue rightTarget = getTarget(shrs.getRight()) == null ? TargetValue.getUnknown() : getTarget(shrs.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shrs, leftTarget.shrs(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shrs, TargetValue.getBad());
		} else {
			setTargetValue(shrs, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTarget(shrs))) {
			workNodes(tempSuccessor.get(shrs));
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
		addSuccessor(sub, sub.getPreds());
		TargetValue target = getTarget(sub);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTarget(sub.getLeft()) == null ? TargetValue.getUnknown() : getTarget(sub.getLeft());
		TargetValue rightTarget = getTarget(sub.getRight()) == null ? TargetValue.getUnknown() : getTarget(sub.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(sub, leftTarget.sub(rightTarget, sub.getMode()));
		} else if (leftTarget.equals(TargetValue.getUnknown()) || rightTarget.equals(TargetValue.getUnknown())) {
			setTargetValue(sub, TargetValue.getUnknown());
		} else {
			setTargetValue(sub, TargetValue.getBad());
		}

		if (target == null || !target.equals(getTarget(sub))) {
			workNodes(tempSuccessor.get(sub));
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
