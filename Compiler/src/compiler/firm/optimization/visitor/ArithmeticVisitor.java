package compiler.firm.optimization.visitor;

import compiler.firm.optimization.Target;

import firm.Mode;
import firm.TargetValue;
import firm.nodes.Add;
import firm.nodes.Const;
import firm.nodes.Div;
import firm.nodes.Mul;
import firm.nodes.Sub;

public class ArithmeticVisitor extends OptimizationVisitor {

	public static OptimizationVisitorFactory getFactory() {
		return new OptimizationVisitorFactory() {
			@Override
			public OptimizationVisitor create() {
				return new ArithmeticVisitor();
			}
		};
	}

	@Override
	public void visit(Add add) {
		TargetValue leftTarget = getTargetValue(add.getLeft());
		TargetValue rightTarget = getTargetValue(add.getRight());

		// reduce x = y + 0 if possible
		if (leftTarget.isNull()) {
			targets.put(add, new Target(add.getRight()));
		} else if (rightTarget.isNull()) {
			targets.put(add, new Target(add.getLeft()));
		}
	}

	@Override
	public void visit(Div division) {
		TargetValue rightTargetValue = getTargetValue(division.getRight());

		// reduce x = y / 1 if possible
		if (rightTargetValue.isOne()) {
			targets.put(getFirstSuccessor(division), new Target(division.getLeft()));
		} else {
			targets.remove(getFirstSuccessor(division));
		}
	}

	@Override
	public void visit(Mul multiplication) {
		TargetValue leftTarget = getTargetValue(multiplication.getLeft());
		TargetValue rightTarget = getTargetValue(multiplication.getRight());

		// reduce x = y * 1 if possible
		if (leftTarget.isOne()) {
			targets.put(multiplication, new Target(multiplication.getRight()));
		} else if (rightTarget.isOne()) {
			targets.put(multiplication, new Target(multiplication.getLeft()));
		}
	}

	@Override
	public void visit(Sub sub) {
		TargetValue rightTarget = getTargetValue(sub.getRight());
		// reduce x = y - 0 if possible
		if (rightTarget.isNull()) {
			targets.put(sub, new Target(sub.getLeft()));
		} else {
			targets.remove(sub);
		}
	}

	@Override
	public void visit(Const constant) {
		if (constant.getMode().equals(Mode.getIs()) || constant.getMode().equals(Mode.getBu()) || constant.getMode().equals(Mode.getLu())) {
			setTargetValue(constant, constant.getTarval(), true);
		}
	}
}
