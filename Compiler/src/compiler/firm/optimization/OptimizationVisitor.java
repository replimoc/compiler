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
	private final HashMap<Node, Node> arithmeticTarget = new HashMap<>();
	// Div and Mod nodes have a Proj successor which must be replaced instead of the Div and Mod nodes themselves
	private final HashMap<Node, Target> specialProjDivModTargets = new HashMap<>();

	public OptimizationVisitor(LinkedList<Node> workList) {
		this.workList = workList;
	}

	public HashMap<Node, Target> getTargetValues() {
		return targets;
	}

	public HashMap<Node, Node> getArithmeticTargets() {
		return arithmeticTarget;
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

	private boolean fixpointReached(Target oldTarget, Node node) {
		if (node instanceof Div || node instanceof Mod) {
			if (oldTarget == null || !oldTarget.equals(specialProjDivModTargets.get(node))) {
				for (Edge edge : BackEdges.getOuts(node)) {
					workNode(edge.node);
				}
				return false;
			}
			return true;
		} else {
			if (oldTarget == null || !oldTarget.equals(getTarget(node))) {
				for (Edge edge : BackEdges.getOuts(node)) {
					workNode(edge.node);
				}
				return false;
			}
			return true;
		}
	}

	private void setTargetValue(Node node, TargetValue targetValue) {
		setTargetValue(node, targetValue, false);
	}

	private void setTargetValue(Node node, TargetValue targetValue, boolean remove) {
		targets.put(node, new Target(targetValue, remove));
	}

	private TargetValue getTargetValue(Node node) {
		return targets.get(node) == null ? TargetValue.getUnknown() : targets.get(node).getTargetValue();
	}

	private Target getTarget(Node node) {
		return targets.get(node);
	}

	private Node getFirstSuccessor(Node node) {
		for (Edge edge : BackEdges.getOuts(node)) {
			return edge.node;
		}
		return null;
	}

	private boolean areConstant(Node... nodes) {
		for (Node n : nodes) {
			Target tar = getTarget(n);
			if (tar == null || !tar.isConstant()) {
				return false;
			}
		}
		return true;
	}

	private void biTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(node, newTargetValue);
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void unaryTransferFunction(Node node, TargetValue newTargetValue) {
		if (newTargetValue.isConstant()) {
			setTargetValue(node, newTargetValue);
		} else if (newTargetValue.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void divModTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue) {
		if (leftTarget.isNull()) {
			specialProjDivModTargets.put(node, new Target(leftTarget));
		} else if (leftTarget.isConstant() && rightTarget.isConstant()) {
			specialProjDivModTargets.put(node, new Target(newTargetValue));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			specialProjDivModTargets.put(node, new Target(TargetValue.getBad()));
		} else {
			specialProjDivModTargets.put(node, new Target(TargetValue.getUnknown()));
		}
	}

	private boolean binaryExpressionCleanup(Node node, Node left, Node right, Target oldTarget) {
		if (fixpointReached(oldTarget, node)) {
			// are we finished?
			if (oldTarget.getTargetValue().isConstant()) {
				setTargetValue(node, oldTarget.getTargetValue(), areConstant(left, right));
			}
			return fixpointReached(oldTarget, node);
		}
		return false;
	}

	private void unaryExpressionCleanup(Node node, Node operand, Target oldTarget) {
		if (fixpointReached(oldTarget, node)) {
			// are we finished?
			if (oldTarget.getTargetValue().isConstant()) {
				setTargetValue(node, oldTarget.getTargetValue(), areConstant(operand));
			}
			fixpointReached(oldTarget, node);
		}
	}

	@Override
	public void visit(Add add) {
		Target oldTarget = getTarget(add);
		TargetValue target = getTargetValue(add);
		TargetValue leftTarget = getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.add(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(add, leftTarget, rightTarget, newTargetValue);

		boolean fixpoint = binaryExpressionCleanup(add, add.getLeft(), add.getRight(), oldTarget);

		if (fixpoint && !target.isConstant()) {
			// reduce x = y + 0 if possible
			if (leftTarget.isNull() && areConstant(add.getLeft())) {
				arithmeticTarget.put(add, add.getRight());
			} else if (rightTarget.isNull() && areConstant(add.getRight())) {
				arithmeticTarget.put(add, add.getLeft());
			} else {
				arithmeticTarget.remove(add);
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
		// nothing to fold
	}

	@Override
	public void visit(Anchor arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(And and) {
		Target oldTarget = getTarget(and);
		TargetValue leftTarget = getTargetValue(and.getLeft());
		TargetValue rightTarget = getTargetValue(and.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.and(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(and, leftTarget, rightTarget, newTargetValue);
		binaryExpressionCleanup(and, and.getLeft(), and.getRight(), oldTarget);
	}

	@Override
	public void visit(Bad arg0) {
		// nothing to fold
	}

	@Override
	public void visit(Bitcast arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Block arg0) {
		// nothing to fold
	}

	@Override
	public void visit(Builtin arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Call arg0) {
		// nothing to fold
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
		if (constant.getMode().equals(Mode.getIs()) || constant.getMode().equals(Mode.getBu()) || constant.getMode().equals(Mode.getLu())) {
			setTargetValue(constant, constant.getTarval(), true);
		}
	}

	@Override
	public void visit(Conv conv) {
		Target oldTarget = getTarget(conv);
		TargetValue target = getTargetValue(conv.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.convertTo(conv.getMode());

		unaryTransferFunction(conv, newTargetValue);
		unaryExpressionCleanup(conv, conv.getOp(), oldTarget);
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
		Target oldTarget = specialProjDivModTargets.get(div);
		TargetValue target = getTargetValue(div);
		Target leftTarget = getTarget(div.getLeft());
		TargetValue leftTargetValue = leftTarget == null ? TargetValue.getUnknown() : leftTarget.getTargetValue();
		TargetValue rightTargetValue = getTargetValue(div.getRight());
		TargetValue newTargetValue;

		if (leftTargetValue.isNull()) {
			newTargetValue = leftTargetValue;
		} else {
			newTargetValue = (leftTargetValue.isConstant() && rightTargetValue.isConstant()) ? leftTargetValue.div(rightTargetValue) : TargetValue
					.getUnknown();
		}

		divModTransferFunction(div, leftTargetValue, rightTargetValue, newTargetValue);

		if (fixpointReached(oldTarget, div)) {
			// are we finished?
			if (target.isConstant()) {
				if (areConstant(div.getLeft(), div.getRight())
						|| (areConstant(div.getLeft()) && targets.get(div.getLeft()).getTargetValue().isNull())) {
					specialProjDivModTargets.put(div, new Target(oldTarget.getTargetValue(), true));
				} else {
					specialProjDivModTargets.put(div, new Target(oldTarget.getTargetValue(), false));
				}
				fixpointReached(oldTarget, div);
			} else {
				// reduce x = y / 1 if possible
				if (rightTargetValue.isOne() && areConstant(div.getRight())) {
					arithmeticTarget.put(getFirstSuccessor(div), div.getLeft());
				} else {
					arithmeticTarget.remove(getFirstSuccessor(div));
				}
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
		// nothing to fold
	}

	@Override
	public void visit(IJmp arg0) {
		// nothing to fold
	}

	@Override
	public void visit(Id arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Jmp arg0) {
		// nothing to fold
	}

	@Override
	public void visit(Load arg0) {
		// nothing to fold
	}

	@Override
	public void visit(Member arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Minus minus) {
		Target oldTarget = getTarget(minus);
		TargetValue target = getTargetValue(minus.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.neg();

		unaryTransferFunction(minus, newTargetValue);
		unaryExpressionCleanup(minus, minus.getOp(), oldTarget);
	}

	@Override
	public void visit(Mod mod) {
		// // firm automatically skips exchanging the node if right target is null
		Target oldTarget = specialProjDivModTargets.get(mod);
		TargetValue target = getTargetValue(mod);
		Target leftTarget = getTarget(mod.getLeft());
		TargetValue leftTargetValue = leftTarget == null ? TargetValue.getUnknown() : leftTarget.getTargetValue();
		TargetValue rightTargetValue = getTargetValue(mod.getRight());

		if (leftTargetValue.isConstant() && rightTargetValue.isConstant()) {
			specialProjDivModTargets.put(mod, new Target(leftTargetValue.mod(rightTargetValue)));
		} else if (leftTargetValue.isNull() || rightTargetValue.isOne()) {
			specialProjDivModTargets.put(mod, new Target(new TargetValue(0, mod.getRight().getMode())));
		} else if (leftTargetValue.equals(TargetValue.getBad()) || rightTargetValue.equals(TargetValue.getBad())) {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getBad()));
		} else {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getUnknown()));
		}

		if (fixpointReached(oldTarget, mod)) {
			// are we finished?
			if (target.isConstant()) {
				if (areConstant(mod.getLeft(), mod.getRight())
						|| (areConstant(mod.getLeft()) && getTargetValue(mod.getLeft()).isNull()) ||
						(areConstant(mod.getRight()) && getTargetValue(mod.getRight()).isOne())) {
					specialProjDivModTargets.put(mod, new Target(oldTarget.getTargetValue(), true));
				} else {
					specialProjDivModTargets.put(mod, new Target(oldTarget.getTargetValue(), false));
				}
				fixpointReached(oldTarget, mod);
			}
		}
	}

	@Override
	public void visit(Mul mul) {
		Target oldTarget = getTarget(mul);
		TargetValue target = getTargetValue(mul);
		TargetValue leftTarget = getTargetValue(mul.getLeft());
		TargetValue rightTarget = getTargetValue(mul.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.mul(rightTarget) : TargetValue.getUnknown();

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(mul, newTargetValue);
		} else if (leftTarget.isNull() || rightTarget.isNull()) {
			setTargetValue(mul, new TargetValue(0, mul.getMode()));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(mul, TargetValue.getBad());
		} else {
			setTargetValue(mul, TargetValue.getUnknown());
		}

		if (fixpointReached(oldTarget, mul)) {
			// are we finished?
			if (target.isConstant()) {
				if (leftTarget.isNull()) {
					setTargetValue(mul, target, areConstant(mul.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(mul, target, areConstant(mul.getRight()));
				} else {
					setTargetValue(mul, target, areConstant(mul.getLeft(), mul.getRight()));
				}
			} else {
				if (leftTarget.isNull()) {
					setTargetValue(mul, target, areConstant(mul.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(mul, target, areConstant(mul.getRight()));
				}
				// reduce x = y * 1 if possible
				if (leftTarget.isOne() && areConstant(mul.getLeft())) {
					arithmeticTarget.put(mul, mul.getRight());
				} else if (rightTarget.isOne() && areConstant(mul.getRight())) {
					arithmeticTarget.put(mul, mul.getLeft());
				} else {
					arithmeticTarget.remove(mul);
				}
			}
			fixpointReached(oldTarget, mul);
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
		// nothing to fold
	}

	@Override
	public void visit(Not not) {
		Target oldTarget = getTarget(not);
		TargetValue target = getTargetValue(not.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.not();

		unaryTransferFunction(not, newTargetValue);
		unaryExpressionCleanup(not, not.getOp(), oldTarget);
	}

	@Override
	public void visit(Offset arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Or or) {
		Target oldTarget = getTarget(or);
		TargetValue leftTarget = getTargetValue(or.getLeft());
		TargetValue rightTarget = getTargetValue(or.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.or(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(or, leftTarget, rightTarget, newTargetValue);
		binaryExpressionCleanup(or, or.getLeft(), or.getRight(), oldTarget);
	}

	@Override
	public void visit(Phi phi) {
		Target oldTarget = getTarget(phi);
		TargetValue target = getTargetValue(phi);
		Target predTarget = getTarget(phi.getPred(0));
		TargetValue predTargetValue = predTarget == null ? TargetValue.getUnknown() : predTarget.getTargetValue();

		for (int i = 1; i < phi.getPredCount(); i++) {
			Target tmpTarget = getTarget(phi.getPred(i));
			TargetValue tmpTargetValue = tmpTarget == null ? TargetValue.getUnknown() : tmpTarget.getTargetValue();
			if (predTargetValue.isConstant() && tmpTargetValue.isConstant() && predTargetValue.equals(tmpTargetValue)) {
				setTargetValue(phi, predTargetValue, true);
			} else if (predTargetValue.equals(TargetValue.getBad()) || tmpTargetValue.equals(TargetValue.getBad())
					|| (predTargetValue.isConstant() && tmpTargetValue.isConstant() && !predTargetValue.equals(tmpTargetValue))) {
				setTargetValue(phi, TargetValue.getBad());
			} else if (tmpTargetValue.equals(TargetValue.getUnknown())) {
				setTargetValue(phi, predTargetValue);
			} else {
				setTargetValue(phi, tmpTargetValue);
				predTargetValue = tmpTargetValue;
			}
		}

		if (fixpointReached(oldTarget, phi)) {
			// are we finished?
			if (target.isConstant()) {
				boolean remove = true;
				// all predecessors constant?
				for (int i = 0; i < phi.getPredCount(); i++) {
					Target tar = getTarget(phi.getPred(i));
					if (tar == null) {
						remove = false;
					} else {
						remove = remove && tar.isConstant();
					}
				}
				if (!remove) {
					setTargetValue(phi, target, false);
				} else {
					setTargetValue(phi, target, true);
				}
				fixpointReached(oldTarget, phi);
			}
		}
	}

	@Override
	public void visit(Pin arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Proj proj) {
		Target oldTarget = getTarget(proj);
		TargetValue target = getTargetValue(proj);
		if (target == null) {
			setTargetValue(proj, TargetValue.getUnknown());
		}
		if (proj.getPredCount() == 1) {
			if (specialProjDivModTargets.containsKey(proj.getPred(0))) {
				Target tar = specialProjDivModTargets.get(proj.getPred(0));
				if (tar != null) {
					TargetValue tarVal = tar.getTargetValue();
					if (tarVal != null) {
						setTargetValue(proj, tarVal, true);
						// we need to visit this node again to check if the div/mod will be removed
					} else {
						setTargetValue(proj, TargetValue.getUnknown());
					}
				}
			}
		}

		if (fixpointReached(oldTarget, proj)) {
			// are we finished?
			if (target.isConstant()) {
				boolean remove = true;
				for (Node pred : proj.getPreds()) {
					for (Node pred2 : pred.getPreds()) {
						Target tar = specialProjDivModTargets.get(pred2);
						if (tar != null) {
							remove = remove && tar.isConstant();
						} else {
							tar = targets.get(pred2);
							if (tar != null) {
								remove = remove && tar.isConstant();
							}
						}
					}
				}
				if (!remove) {
					setTargetValue(proj, target, false);
				} else {
					setTargetValue(proj, target, true);
				}
				fixpointReached(oldTarget, proj);
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
		// nothing to fold
	}

	@Override
	public void visit(Shl shl) {
		Target oldTarget = getTarget(shl);
		TargetValue leftTarget = getTargetValue(shl.getLeft());
		TargetValue rightTarget = getTargetValue(shl.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shl(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shl, leftTarget, rightTarget, newTargetValue);
		binaryExpressionCleanup(shl, shl.getLeft(), shl.getRight(), oldTarget);
	}

	@Override
	public void visit(Shr shr) {
		Target oldTarget = getTarget(shr);
		TargetValue leftTarget = getTargetValue(shr.getLeft());
		TargetValue rightTarget = getTargetValue(shr.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shr(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shr, leftTarget, rightTarget, newTargetValue);
		binaryExpressionCleanup(shr, shr.getLeft(), shr.getRight(), oldTarget);
	}

	@Override
	public void visit(Shrs shrs) {
		Target oldTarget = getTarget(shrs);
		TargetValue leftTarget = getTargetValue(shrs.getLeft());
		TargetValue rightTarget = getTargetValue(shrs.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shrs(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shrs, leftTarget, rightTarget, newTargetValue);
		binaryExpressionCleanup(shrs, shrs.getLeft(), shrs.getRight(), oldTarget);
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
		Target oldTarget = getTarget(sub);
		TargetValue target = getTargetValue(sub);
		TargetValue leftTarget = getTargetValue(sub.getLeft());
		TargetValue rightTarget = getTargetValue(sub.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.sub(rightTarget, sub.getMode()) : TargetValue
				.getUnknown();

		biTransferFunction(sub, leftTarget, rightTarget, newTargetValue);
		boolean fixpoint = binaryExpressionCleanup(sub, sub.getLeft(), sub.getRight(), oldTarget);

		if (fixpoint && !target.isConstant()) {
			// reduce x = y - 0 if possible
			if (rightTarget.isNull() && areConstant(sub.getRight())) {
				arithmeticTarget.put(sub, sub.getLeft());
			} else {
				arithmeticTarget.remove(sub);
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
		// nothing to fold
	}

	@Override
	public void visitUnknown(Node arg0) {
		// nothing to fold
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
		private boolean constant;

		public Target(TargetValue target, boolean remove) {
			this.target = target;
			this.constant = remove;
		}

		public Target(TargetValue target) {
			this(target, false);
		}

		public TargetValue getTargetValue() {
			return target;
		}

		public boolean isConstant() {
			return constant;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (constant ? 1231 : 1237);
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Target other = (Target) obj;
			if (constant != other.constant)
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}

	}

}
