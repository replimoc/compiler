package compiler.firm.optimization;

import java.util.HashMap;

import compiler.firm.FirmUtils;

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

	private final HashMap<Node, Target> targets = new HashMap<>();
	// Div and Mod nodes have a Proj successor which must be replaced instead of the Div and Mod nodes themselves
	private final HashMap<Node, Target> specialProjDivModTargets = new HashMap<>();

	public HashMap<Node, Target> getTargetValues() {
		return targets;
	}

	private boolean fixpointReached(Target oldTarget, Node node) {
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

	private boolean hasFixpointReached(Node... nodes) {
		for (Node n : nodes) {
			Target tar = getTarget(n);
			if (tar == null || !tar.isFixpointReached()) {
				return false;
			}
		}
		return true;
	}

	private void biTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue, Node left, Node right) {
		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(node, newTargetValue, hasFixpointReached(left, right));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void unaryTransferFunction(Node node, TargetValue newTargetValue, Node operand) {
		if (newTargetValue.isConstant()) {
			setTargetValue(node, newTargetValue, hasFixpointReached(operand));
		} else if (newTargetValue.equals(TargetValue.getBad())) {
			setTargetValue(node, TargetValue.getBad());
		} else {
			setTargetValue(node, TargetValue.getUnknown());
		}
	}

	private void divModTransferFunction(Node node, TargetValue leftTarget, TargetValue rightTarget, TargetValue newTargetValue, Node left, Node right) {
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
		TargetValue target = getTargetValue(add);
		TargetValue leftTarget = getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.add(rightTarget) : TargetValue.getUnknown();

		biTransferFunction(add, leftTarget, rightTarget, newTargetValue, add.getLeft(), add.getRight());

		boolean fixpoint = binaryExpressionCleanup(add, add.getLeft(), add.getRight(), oldTarget);

		if (fixpoint && !target.isConstant()) {
			// reduce x = y + 0 if possible
			if (leftTarget.isNull() && hasFixpointReached(add.getLeft())) {
				targets.put(add, new Target(add.getRight()));
			} else if (rightTarget.isNull() && hasFixpointReached(add.getRight())) {
				targets.put(add, new Target(add.getLeft()));
			} else {
				targets.remove(add);
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

		biTransferFunction(and, leftTarget, rightTarget, newTargetValue, and.getLeft(), and.getRight());
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

	/**
	 * Control flow optimization
	 * 
	 * @author Valentin Zickner
	 */
	@Override
	public void visit(Cmp compare) {
		Node left = compare.getLeft();
		Node right = compare.getRight();

		TargetValue leftTargetValue = getTargetValue(left);
		TargetValue rightTargetValue = getTargetValue(right);

		if (hasFixpointReached(left, right) && leftTargetValue.isConstant() && rightTargetValue.isConstant()) {
			boolean result = false;
			boolean success = true;
			int leftInt = getInteger(leftTargetValue);
			int rightInt = getInteger(rightTargetValue);
			switch (compare.getRelation()) {
			case Equal:
				result = leftInt == rightInt;
				break;
			case LessGreater:
				result = leftInt != rightInt;
				break;
			case Less:
				result = leftInt < rightInt;
				break;
			case Greater:
				result = leftInt > rightInt;
				break;
			case LessEqual:
				result = leftInt <= rightInt;
				break;
			case GreaterEqual:
				result = leftInt >= rightInt;
				break;
			default:
				success = false;
				break;
			}
			TargetValue target = TargetValue.getBad();
			if (success) {
				target = result ? TargetValue.getBTrue() : TargetValue.getBFalse();
			}
			setTargetValue(compare, target);
		} else {
			setTargetValue(compare, TargetValue.getBad());
		}

	}

	private int getInteger(TargetValue value) {
		String result = new StringBuilder(value.getBitpattern()).reverse().toString();
		String negative = result.substring(1);
		if (result.charAt(0) == '1') {
			return (int) -(Math.pow(2, negative.length()) - Math.abs(Integer.parseInt(negative, 2)));
		} else {
			return Integer.parseInt(result, 2);
		}
	}

	/**
	 * Control flow optimization
	 * 
	 * @author Valentin Zickner
	 */
	@Override
	public void visit(Cond condition) {
		boolean eliminate = true;
		boolean useCase = false;

		for (Node pred : condition.getPreds()) {
			eliminate &= getTargetValue(pred).isConstant();
			useCase = getTargetValue(pred).isOne();
		}

		if (eliminate) {
			for (Edge edge : BackEdges.getOuts(condition)) {

				Proj proj = (Proj) edge.node;

				if ((proj.getNum() == FirmUtils.TRUE) == useCase) {
					Block block = (Block) condition.getBlock();
					Node jump = block.getGraph().newJmp(block);
					targets.put(proj, new Target(jump));
				} else {
					targets.put(proj, new Target(proj.getGraph().newBad(proj.getPred().getMode())));
				}
			}
		}
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

		unaryTransferFunction(conv, newTargetValue, conv.getOp());
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

		divModTransferFunction(div, leftTargetValue, rightTargetValue, newTargetValue, div.getLeft(), div.getRight());

		if (fixpointReached(oldTarget, div)) {
			// are we finished?
			if (target.isConstant()) {
				if (hasFixpointReached(div.getLeft(), div.getRight())
						|| (hasFixpointReached(div.getLeft()) && getTargetValue(div.getLeft()).isNull())) {
					specialProjDivModTargets.put(div, new Target(oldTarget.getTargetValue(), true));
				} else {
					specialProjDivModTargets.put(div, new Target(oldTarget.getTargetValue(), false));
				}
				fixpointReached(oldTarget, div);
			} else {
				// reduce x = y / 1 if possible
				if (rightTargetValue.isOne() && hasFixpointReached(div.getRight())) {
					targets.put(getFirstSuccessor(div), new Target(div.getLeft()));
				} else {
					targets.remove(getFirstSuccessor(div));
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
	public void visit(Mul mul) {
		Target oldTarget = getTarget(mul);
		TargetValue target = getTargetValue(mul);
		TargetValue leftTarget = getTargetValue(mul.getLeft());
		TargetValue rightTarget = getTargetValue(mul.getRight());
		TargetValue newTargetValue = (leftTarget.isConstant() && rightTarget.isConstant()) ? leftTarget.mul(rightTarget) : TargetValue.getUnknown();

		if (leftTarget.isConstant() && rightTarget.isConstant()) {
			setTargetValue(mul, newTargetValue, hasFixpointReached(mul.getLeft(), mul.getRight()));
		} else if (leftTarget.isNull()) {
			setTargetValue(mul, new TargetValue(0, mul.getMode()), hasFixpointReached(mul.getLeft()));
		} else if (rightTarget.isNull()) {
			setTargetValue(mul, new TargetValue(0, mul.getMode()), hasFixpointReached(mul.getRight()));
		} else if (leftTarget.equals(TargetValue.getBad()) || rightTarget.equals(TargetValue.getBad())) {
			setTargetValue(mul, TargetValue.getBad());
		} else {
			setTargetValue(mul, TargetValue.getUnknown());
		}

		if (fixpointReached(oldTarget, mul)) {
			// are we finished?
			if (target.isConstant()) {
				if (leftTarget.isNull()) {
					setTargetValue(mul, target, hasFixpointReached(mul.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(mul, target, hasFixpointReached(mul.getRight()));
				} else {
					setTargetValue(mul, target, hasFixpointReached(mul.getLeft(), mul.getRight()));
				}
			} else {
				if (leftTarget.isNull()) {
					setTargetValue(mul, target, hasFixpointReached(mul.getLeft()));
				} else if (rightTarget.isNull()) {
					setTargetValue(mul, target, hasFixpointReached(mul.getRight()));
				}
				// reduce x = y * 1 if possible
				if (leftTarget.isOne() && hasFixpointReached(mul.getLeft())) {
					targets.put(mul, new Target(mul.getRight()));
				} else if (rightTarget.isOne() && hasFixpointReached(mul.getRight())) {
					targets.put(mul, new Target(mul.getLeft()));
				} else {
					targets.remove(mul);
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

		unaryTransferFunction(not, newTargetValue, not.getOp());
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

}
