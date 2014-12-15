package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.Map.Entry;

import compiler.firm.optimization.Target;

import firm.BackEdges;
import firm.BackEdges.Edge;
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
import firm.nodes.Minus;
import firm.nodes.Mod;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Or;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Sub;

public class ConstantFoldingVisitor extends OptimizationVisitor {

	protected final HashMap<Node, Target> targets = new HashMap<>();
	// Div and Mod nodes have a Proj successor which must be replaced instead of the Div and Mod nodes themselves
	protected final HashMap<Node, Target> specialProjDivModTargets = new HashMap<>();

	@Override
	public HashMap<Node, Node> getNodeReplacements() {
		HashMap<Node, Node> replacements = new HashMap<>();

		for (Entry<Node, Target> targetEntry : targets.entrySet()) {
			Node node = targetEntry.getKey();
			Target target = targetEntry.getValue();
			if (target.isNode()) {
				replacements.put(node, target.getNode());
			} else {
				if (target.isFixpointReached() && target.getTargetValue().isConstant()) {
					replacements.put(node, node.getGraph().newConst(target.getTargetValue()));
				}
			}
		}
		return replacements;
	}

	protected boolean fixpointReached(Target oldTarget, Node node) {
		if (node instanceof Div || node instanceof Mod) {
			if (oldTarget == null || !oldTarget.equals(specialProjDivModTargets.get(node))) {
				return false;
			}
			return true;
		} else {
			if (oldTarget == null || !oldTarget.equals(getTarget(node))) {
				return false;
			}
			return true;
		}
	}

	protected void setTargetValue(Node node, TargetValue targetValue) {
		setTargetValue(node, targetValue, false);
	}

	protected void setTargetValue(Node node, TargetValue targetValue, boolean remove) {
		targets.put(node, new Target(targetValue, remove));
	}

	protected TargetValue getTargetValue(Node node) {
		return targets.get(node) == null ? TargetValue.getUnknown() : targets.get(node).getTargetValue();
	}

	protected Target getTarget(Node node) {
		return targets.get(node);
	}

	public HashMap<Node, Target> getTargetValues() {
		return targets;
	}

	public static OptimizationVisitorFactory getFactory() {
		return new OptimizationVisitorFactory() {
			@Override
			public OptimizationVisitor create() {
				return new ConstantFoldingVisitor();
			}
		};
	}

	protected boolean hasFixpointReached(Node... nodes) {
		for (Node n : nodes) {
			Target tar = getTarget(n);
			if (tar == null || !tar.isFixpointReached()) {
				return false;
			}
		}
		return true;
	}

	protected void biTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue, Node left, Node right) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(node, newTargetValue, hasFixpointReached(left, right));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	protected void unaryTransferFunction(Node node, TargetValue newTargetValue, Node operand) {
		if (newTargetValue.isConstant()) {
			setTargetValue(node, newTargetValue, hasFixpointReached(operand));
		} else if (newTargetValue.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	protected void divModTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue, Node left,
			Node right) {
		if (leftTarget.isNull()) {
			specialProjDivModTargets.put(node, new Target(leftTarget, hasFixpointReached(left)));
		} else if (leftTarget.isConstant() && rightTarget.isConstant()) {
			specialProjDivModTargets.put(node, new Target(newTargetValue, hasFixpointReached(left, right)));
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
				setTargetValue(node, oldTarget.getTargetValue(), hasFixpointReached(left, right));
			}
			return fixpointReached(oldTarget, node);
		}
		return false;
	}

	private void unaryExpressionCleanup(Node node, Node operand, Target oldTarget) {
		if (fixpointReached(oldTarget, node)) {
			// are we finished?
			if (oldTarget.getTargetValue().isConstant()) {
				setTargetValue(node, oldTarget.getTargetValue(), hasFixpointReached(operand));
			}
			fixpointReached(oldTarget, node);
		}
	}

	@Override
	public void visit(Add add) {
		Target oldTarget = getTarget(add);
		TargetValue leftTarget = getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.add(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(add, leftTarget, rightTarget, newTargetValue, add.getLeft(), add.getRight());

		binaryExpressionCleanup(add, add.getLeft(), add.getRight(), oldTarget);
	}

	@Override
	public void visit(And and) {
		Target oldTarget = getTarget(and);
		TargetValue leftTarget = getTargetValue(and.getLeft());
		TargetValue rightTarget = getTargetValue(and.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.and(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(and, leftTarget, rightTarget, newTargetValue, and.getLeft(), and.getRight());
		binaryExpressionCleanup(and, and.getLeft(), and.getRight(), oldTarget);
	}

	@Override
	public void visit(Const constant) {
		if (isConstant(constant)) {
			setTargetValue(constant, constant.getTarval(), true);
		}
	}

	@Override
	public void visit(Conv convertion) {
		Target oldTarget = getTarget(convertion);
		TargetValue target = getTargetValue(convertion.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.convertTo(convertion.getMode());

		unaryTransferFunction(convertion, newTargetValue, convertion.getOp());
		unaryExpressionCleanup(convertion, convertion.getOp(), oldTarget);
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
	public void visit(CopyB arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Deleted arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Div division) {
		// // firm automatically skips exchanging the node if right target is null
		Target oldTarget = specialProjDivModTargets.get(division);
		TargetValue target = getTargetValue(division);
		Target leftTarget = getTarget(division.getLeft());
		TargetValue leftTargetValue = leftTarget == null ? TargetValue.getUnknown() : leftTarget.getTargetValue();
		TargetValue rightTargetValue = getTargetValue(division.getRight());
		TargetValue newTargetValue;

		if (leftTargetValue.isNull()) {
			newTargetValue = leftTargetValue;
		} else {
			newTargetValue = (leftTargetValue.isConstant() && rightTargetValue.isConstant()) ? leftTargetValue.div(rightTargetValue) : TargetValue
					.getUnknown();
		}

		divModTransferFunction(division, leftTargetValue, rightTargetValue, newTargetValue, division.getLeft(), division.getRight());

		if (target.isConstant()) {
			if (hasFixpointReached(division.getLeft(), division.getRight())
					|| (hasFixpointReached(division.getLeft()) && getTargetValue(division.getLeft()).isNull())) {
				specialProjDivModTargets.put(division, new Target(oldTarget.getTargetValue(), true));
			} else {
				specialProjDivModTargets.put(division, new Target(oldTarget.getTargetValue(), false));
			}
			fixpointReached(oldTarget, division);
		}
	}

	@Override
	public void visit(Minus minus) {
		Target oldTarget = getTarget(minus);
		TargetValue target = getTargetValue(minus.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.neg();

		unaryTransferFunction(minus, newTargetValue, minus.getOp());
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
			specialProjDivModTargets.put(mod, new Target(leftTargetValue.mod(rightTargetValue), hasFixpointReached(mod.getLeft(), mod.getRight())));
		} else if (leftTargetValue.isNull()) {
			specialProjDivModTargets.put(mod, new Target(new TargetValue(0, mod.getLeft().getMode()), hasFixpointReached(mod.getLeft())));
		} else if (rightTargetValue.isOne()) {
			specialProjDivModTargets.put(mod, new Target(new TargetValue(0, mod.getRight().getMode()), hasFixpointReached(mod.getRight())));
		} else if (leftTargetValue.equals(TargetValue.getBad()) || rightTargetValue.equals(TargetValue.getBad())) {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getBad()));
		} else {
			specialProjDivModTargets.put(mod, new Target(TargetValue.getUnknown()));
		}

		if (fixpointReached(oldTarget, mod)) {
			// are we finished?
			if (target.isConstant()) {
				if (hasFixpointReached(mod.getLeft(), mod.getRight())
						|| (hasFixpointReached(mod.getLeft()) && getTargetValue(mod.getLeft()).isNull()) ||
						(hasFixpointReached(mod.getRight()) && getTargetValue(mod.getRight()).isOne())) {
					specialProjDivModTargets.put(mod, new Target(oldTarget.getTargetValue(), true));
				} else {
					specialProjDivModTargets.put(mod, new Target(oldTarget.getTargetValue(), false));
				}
				fixpointReached(oldTarget, mod);
			}
		}
	}

	@Override
	public void visit(Mul multiplication) {
		Target oldTarget = getTarget(multiplication);
		TargetValue target = getTargetValue(multiplication);
		TargetValue leftTarget = getTargetValue(multiplication.getLeft());
		TargetValue rightTarget = getTargetValue(multiplication.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.mul(rightTarget) : TargetValue.getUnknown();

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(multiplication, newTargetValue, hasFixpointReached(multiplication.getLeft(), multiplication.getRight()));
		} else if (leftTarget.isNull()) {
			setTargetValue(multiplication, new TargetValue(0, multiplication.getMode()), hasFixpointReached(multiplication.getLeft()));
		} else if (rightTarget.isNull()) {
			setTargetValue(multiplication, new TargetValue(0, multiplication.getMode()), hasFixpointReached(multiplication.getRight()));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(multiplication, TargetValue.getBad());
		} else {
			setTargetValue(multiplication, TargetValue.getUnknown());
		}

		if (fixpointReached(oldTarget, multiplication)) {
			// are we finished?
			if (target.isConstant()) {
				if (leftTarget.isNull()) {
					setTargetValue(multiplication, target, hasFixpointReached(multiplication.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(multiplication, target, hasFixpointReached(multiplication.getRight()));
				} else {
					setTargetValue(multiplication, target, hasFixpointReached(multiplication.getLeft(), multiplication.getRight()));
				}
			} else {
				if (leftTarget.isNull()) {
					setTargetValue(multiplication, target, hasFixpointReached(multiplication.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(multiplication, target, hasFixpointReached(multiplication.getRight()));
				}
			}
			fixpointReached(oldTarget, multiplication);
		}
	}

	@Override
	public void visit(Not not) {
		Target oldTarget = getTarget(not);
		TargetValue target = getTargetValue(not.getOp());
		TargetValue newTargetValue = (target == null || !target.isConstant()) ? TargetValue.getUnknown() : target.not();

		unaryTransferFunction(not, newTargetValue, not.getOp());
		unaryExpressionCleanup(not, not.getOp(), oldTarget);
	}

	@Override
	public void visit(Or or) {
		Target oldTarget = getTarget(or);
		TargetValue leftTarget = getTargetValue(or.getLeft());
		TargetValue rightTarget = getTargetValue(or.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.or(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(or, leftTarget, rightTarget, newTargetValue, or.getLeft(), or.getRight());
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
						remove = remove && tar.isFixpointReached();
					}
				}
				if (!remove) {
					setTargetValue(phi, target, false);
				} else {
					setTargetValue(phi, target, true);
				}
				if (oldTarget.isFixpointReached() && remove != oldTarget.getTargetValue().isConstant()) {
					// visit the depending nodes only once more
					for (Edge edge : BackEdges.getOuts(phi)) {
						edge.node.accept(this);
					}
				}
			}
		}
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
						setTargetValue(proj, tarVal, hasFixpointReached(proj.getPred(0)));
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
							remove = remove && tar.isFixpointReached();
						} else {
							tar = getTarget(pred2);
							if (tar != null) {
								remove = remove && tar.isFixpointReached();
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
	public void visit(Shl shl) {
		Target oldTarget = getTarget(shl);
		TargetValue leftTarget = getTargetValue(shl.getLeft());
		TargetValue rightTarget = getTargetValue(shl.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shl(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shl, leftTarget, rightTarget, newTargetValue, shl.getLeft(), shl.getRight());
		binaryExpressionCleanup(shl, shl.getLeft(), shl.getRight(), oldTarget);
	}

	@Override
	public void visit(Shr shr) {
		Target oldTarget = getTarget(shr);
		TargetValue leftTarget = getTargetValue(shr.getLeft());
		TargetValue rightTarget = getTargetValue(shr.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shr(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shr, leftTarget, rightTarget, newTargetValue, shr.getLeft(), shr.getRight());
		binaryExpressionCleanup(shr, shr.getLeft(), shr.getRight(), oldTarget);
	}

	@Override
	public void visit(Shrs shrs) {
		Target oldTarget = getTarget(shrs);
		TargetValue leftTarget = getTargetValue(shrs.getLeft());
		TargetValue rightTarget = getTargetValue(shrs.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.shrs(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(shrs, leftTarget, rightTarget, newTargetValue, shrs.getLeft(), shrs.getRight());
		binaryExpressionCleanup(shrs, shrs.getLeft(), shrs.getRight(), oldTarget);
	}

	@Override
	public void visit(Sub sub) {
		Target oldTarget = getTarget(sub);
		TargetValue target = getTargetValue(sub);
		TargetValue leftTarget = getTargetValue(sub.getLeft());
		TargetValue rightTarget = getTargetValue(sub.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.sub(rightTarget, sub.getMode()) : TargetValue
				.getUnknown();

		biTransferFunction(sub, leftTarget, rightTarget, newTargetValue, sub.getLeft(), sub.getRight());
		boolean fixpoint = binaryExpressionCleanup(sub, sub.getLeft(), sub.getRight(), oldTarget);

		if (fixpoint && !target.isConstant()) {
			// reduce x = y - 0 if possible
			if (rightTarget.isNull() && hasFixpointReached(sub.getRight())) {
				targets.put(sub, new Target(sub.getLeft()));
			} else {
				targets.remove(sub);
			}
		}
	}

}
