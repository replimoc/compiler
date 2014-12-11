package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;

import firm.BackEdges;
import firm.BackEdges.Edge;
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
	private final HashMap<Node, Target> targets = new HashMap<>();
	// Div and Mod nodes have a Proj successor which must be replaced instead of the Div and Mod nodes themselves
	private final HashMap<Node, Target> specialProjDivModTargets = new HashMap<>();

	public OptimizationVisitor(LinkedList<Node> workList) {
		this.workList = workList;
	}

	public HashMap<Node, Target> getTargetValues() {
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
		targets.put(node, new Target(targetValue));
	}

	private void setTargetValue(Node node, TargetValue targetValue, boolean remove) {
		targets.put(node, new Target(targetValue, remove));
	}

	private TargetValue getTargetValue(Node node) {
		return targets.get(node) == null ? null : targets.get(node).getTargetValue();
	}

	private Target getTarget(Node node) {
		return targets.get(node);
	}

	@Override
	public void visit(Add add) {
		TargetValue target = getTargetValue(add);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(add.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(add.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(add, leftTarget.add(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(add, TargetValue.getBad());
		} else {
			setTargetValue(add, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(add))) {
			for (Edge edge : BackEdges.getOuts(add)) {
				workNode(edge.node);
			}
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
		TargetValue target = getTargetValue(and);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(and.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(and.getLeft());
		TargetValue rightTarget = getTargetValue(and.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(and.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(and, leftTarget.and(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(and, TargetValue.getBad());
		} else {
			setTargetValue(and, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(and))) {
			for (Edge edge : BackEdges.getOuts(and)) {
				workNode(edge.node);
			}
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
		TargetValue target = getTargetValue(div);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		Target leftTarget = getTarget(div.getLeft());
		TargetValue leftTargetValue = (leftTarget == null || leftTarget.getTargetValue() == null) ? TargetValue.getUnknown() : leftTarget
				.getTargetValue();
		TargetValue rightTargetValue = getTargetValue(div.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(div.getRight());

		if (leftTargetValue.isNull()) {
			if (leftTarget.isRemove()) {
				specialProjDivModTargets.put(div, new Target(leftTargetValue, false));
			} else {
				specialProjDivModTargets.put(div, new Target(leftTargetValue, true));
			}
		} else if (leftTargetValue.isConstant() && rightTargetValue.isConstant()) {
			specialProjDivModTargets.put(div, new Target(leftTargetValue.div(rightTargetValue)));
		} else if (leftTargetValue.equals(TargetValue.getBad()) || rightTargetValue.equals(TargetValue.getBad())) {
			specialProjDivModTargets.put(div, new Target(TargetValue.getBad()));
		} else {
			specialProjDivModTargets.put(div, new Target(TargetValue.getUnknown()));
		}

		if (target == null || !target.equals(getTargetValue(div))) {
			for (Edge edge : BackEdges.getOuts(div)) {
				workNode(edge.node);
			}
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
		TargetValue target = getTargetValue(minus.getOp());
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue newTargetValue = target == null ? TargetValue.getUnknown() : target.neg();

		if (newTargetValue.isConstant()) {
			setTargetValue(minus, newTargetValue);
		} else if (newTargetValue.equals(TargetValue.getBad())) {
			setTargetValue(minus, newTargetValue);
		} else {
			setTargetValue(minus, newTargetValue);
		}

		if (target == null || !target.equals(getTargetValue(minus))) {
			for (Edge edge : BackEdges.getOuts(minus)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Mod mod) {
		// // firm automatically skips exchanging the node if right target is null
		TargetValue target = getTargetValue(mod);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		Target leftTarget = getTarget(mod.getLeft());
		TargetValue leftTargetValue = (leftTarget == null || leftTarget.getTargetValue() == null) ? TargetValue.getUnknown() : leftTarget
				.getTargetValue();
		TargetValue rightTarget = getTargetValue(mod.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(mod.getRight());

		if (leftTargetValue.isNull()) {
			if (leftTarget.isRemove()) {
				specialProjDivModTargets.put(mod, new Target(leftTargetValue, false));
			} else {
				specialProjDivModTargets.put(mod, new Target(leftTargetValue, true));
			}
		} else if (leftTargetValue.isConstant() && rightTarget.isConstant()) {
			specialProjDivModTargets.put(mod, new Target(leftTargetValue.mod(rightTarget)));
		} else if (leftTargetValue.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getBad()));
		} else {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getUnknown()));
		}

		if (target == null || !target.equals(getTargetValue(mod))) {
			for (Edge edge : BackEdges.getOuts(mod)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Mul mul) {
		TargetValue target = getTargetValue(mul);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(mul.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(mul.getLeft());
		TargetValue rightTarget = getTargetValue(mul.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(mul.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(mul, leftTarget.mul(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(mul, TargetValue.getBad());
		} else {
			setTargetValue(mul, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(mul))) {
			for (Edge edge : BackEdges.getOuts(mul)) {
				workNode(edge.node);
			}
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
		TargetValue target = getTargetValue(not.getOp());
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue newTargetValue = target == null ? TargetValue.getUnknown() : target.not();

		if (newTargetValue.isConstant()) {
			setTargetValue(not, newTargetValue);
		} else if (newTargetValue.equals(TargetValue.getBad())) {
			setTargetValue(not, newTargetValue);
		} else {
			setTargetValue(not, newTargetValue);
		}

		if (target == null || !target.equals(getTargetValue(not))) {
			for (Edge edge : BackEdges.getOuts(not)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Offset arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Or or) {
		TargetValue target = getTargetValue(or);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(or.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(or.getLeft());
		TargetValue rightTarget = getTargetValue(or.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(or.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(or, leftTarget.or(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(or, TargetValue.getBad());
		} else {
			setTargetValue(or, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(or))) {
			for (Edge edge : BackEdges.getOuts(or)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Phi phi) {
		TargetValue target = getTargetValue(phi);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}

		Target predTarget = getTarget(phi.getPred(0));
		TargetValue predTargetValue = (predTarget == null || predTarget.getTargetValue() == null) ? TargetValue.getUnknown() : predTarget
				.getTargetValue();
		for (int i = 1; i < phi.getPredCount(); i++) {
			Target tmpTarget = getTarget(phi.getPred(i));
			TargetValue tmpTargetValue = (tmpTarget == null || tmpTarget.getTargetValue() == null) ? TargetValue.getUnknown() : tmpTarget
					.getTargetValue();
			if (predTargetValue.isConstant() && tmpTargetValue.isConstant() && predTargetValue.equals(tmpTargetValue)) {
				if (predTarget.isRemove() && tmpTarget.isRemove()) {
					setTargetValue(phi, predTargetValue, true);
				} else {
					setTargetValue(phi, predTargetValue, false);
				}
			} else if (predTargetValue.equals(TargetValue.getBad()) || tmpTargetValue.equals(TargetValue.getBad())
					|| (predTargetValue.isConstant() && tmpTargetValue.isConstant() && !predTargetValue.equals(tmpTargetValue))) {
				setTargetValue(phi, TargetValue.getBad(), false);
				break;
			} else if (tmpTargetValue.equals(TargetValue.getUnknown())) {
				setTargetValue(phi, predTargetValue, false);
			} else {
				setTargetValue(phi, tmpTargetValue, false);
				predTargetValue = tmpTargetValue;
			}
		}

		if (target == null || !target.equals(getTargetValue(phi))) {
			for (Edge edge : BackEdges.getOuts(phi)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Pin arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Proj proj) {
		TargetValue target = getTargetValue(proj);
		if (proj.getPredCount() == 1) {
			if (specialProjDivModTargets.containsKey(proj.getPred(0))) {
				Target tar = specialProjDivModTargets.get(proj.getPred(0));
				if (tar != null) {
					TargetValue tarVal = tar.getTargetValue();
					if (tarVal != null && tar.isRemove()) {
						setTargetValue(proj, tarVal);
					}
				}
			}
		}

		if (target == null || !target.equals(getTargetValue(proj))) {
			for (Edge edge : BackEdges.getOuts(proj)) {
				workNode(edge.node);
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
		TargetValue target = getTargetValue(shl);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(shl.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(shl.getLeft());
		TargetValue rightTarget = getTargetValue(shl.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(shl.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shl, leftTarget.shl(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shl, TargetValue.getBad());
		} else {
			setTargetValue(shl, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(shl))) {
			for (Edge edge : BackEdges.getOuts(shl)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Shr shr) {
		TargetValue target = getTargetValue(shr);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(shr.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(shr.getLeft());
		TargetValue rightTarget = getTargetValue(shr.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(shr.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shr, leftTarget.shr(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shr, TargetValue.getBad());
		} else {
			setTargetValue(shr, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(shr))) {
			for (Edge edge : BackEdges.getOuts(shr)) {
				workNode(edge.node);
			}
		}
	}

	@Override
	public void visit(Shrs shrs) {
		TargetValue target = getTargetValue(shrs);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(shrs.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(shrs.getLeft());
		TargetValue rightTarget = getTargetValue(shrs.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(shrs.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(shrs, leftTarget.shrs(rightTarget));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(shrs, TargetValue.getBad());
		} else {
			setTargetValue(shrs, TargetValue.getUnknown());
		}

		if (target == null || !target.equals(getTargetValue(shrs))) {
			for (Edge edge : BackEdges.getOuts(shrs)) {
				workNode(edge.node);
			}
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
		TargetValue target = getTargetValue(sub);
		if (target != null && target.equals(TargetValue.getBad())) {
			// no const
			return;
		}
		TargetValue leftTarget = getTargetValue(sub.getLeft()) == null ? TargetValue.getUnknown() : getTargetValue(sub.getLeft());
		TargetValue rightTarget = getTargetValue(sub.getRight()) == null ? TargetValue.getUnknown() : getTargetValue(sub.getRight());

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(sub, leftTarget.sub(rightTarget, sub.getMode()));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(sub, TargetValue.getBad());
		} else {
			setTargetValue(sub, TargetValue.getBad());
		}

		if (target == null || !target.equals(getTargetValue(sub))) {
			for (Edge edge : BackEdges.getOuts(sub)) {
				workNode(edge.node);
			}
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

	/**
	 * Represents a target value for a node.
	 *
	 */
	public class Target {
		/**
		 * Target value
		 */
		private TargetValue target;
		/**
		 * Flag if the node shall be removed from the graph.
		 */
		private boolean remove;

		public Target(TargetValue target, boolean remove) {
			this.target = target;
			this.remove = remove;
		}

		public Target(TargetValue target) {
			this(target, true);
		}

		public TargetValue getTargetValue() {
			return target;
		}

		public boolean isRemove() {
			return remove;
		}
	}

}
