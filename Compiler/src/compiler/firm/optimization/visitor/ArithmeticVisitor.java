package compiler.firm.optimization.visitor;

import firm.TargetValue;
import firm.nodes.Add;
import firm.nodes.Const;
import firm.nodes.Div;
import firm.nodes.Mul;
import firm.nodes.Node;
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

	private TargetValue getTargetValue(Node node) {
		if (node instanceof Const) {
			return ((Const) node).getTarval();
		}
		return null;
	}

	@Override
	public void visit(Add add) {
		Node left = add.getLeft();
		Node right = add.getRight();

		// reduce x = y + 0 if possible
		if (isConstant(left) && getTargetValue(left).isNull()) {
			addReplacement(add, add.getRight());
		} else if (isConstant(right) && getTargetValue(right).isNull()) {
			addReplacement(add, add.getLeft());
		}
	}

	@Override
	public void visit(Div division) {
		Node right = division.getRight();

		// reduce x = y / 1 if possible
		if (isConstant(right) && getTargetValue(right).isOne()) {
			addReplacement(getFirstSuccessor(division), division.getLeft());
		}
	}

	@Override
	public void visit(Mul multiplication) {
		Node left = multiplication.getLeft();
		Node right = multiplication.getRight();

		// reduce x = y * 1 if possible
		if (isConstant(left) && getTargetValue(left).isOne()) {
			addReplacement(multiplication, multiplication.getRight());
		} else if (isConstant(right) && getTargetValue(right).isOne()) {
			addReplacement(multiplication, multiplication.getLeft());
		}
	}

	@Override
	public void visit(Sub sub) {
		Node right = sub.getRight();
		// reduce x = y - 0 if possible
		if (isConstant(right) && getTargetValue(right).isNull()) {
			addReplacement(sub, sub.getLeft());
		}
	}
}
