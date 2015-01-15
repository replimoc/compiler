package compiler.firm.optimization.visitor;

import java.util.HashMap;

import compiler.firm.FirmUtils;

import firm.Mode;
import firm.TargetValue;
import firm.nodes.Add;
import firm.nodes.Const;
import firm.nodes.Conv;
import firm.nodes.Div;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Sub;

public class LocalOptimizationVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LocalOptimizationVisitor();
		}
	};

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	private TargetValue getTargetValue(Node node) {
		if (node instanceof Const) {
			return ((Const) node).getTarval();
		}
		return TargetValue.getUnknown();
	}

	@Override
	public void visit(Add add) {
		Node left = add.getLeft();
		Node right = add.getRight();

		if (left instanceof Conv) {
			left = ((Conv) left).getOp();
		}
		if (right instanceof Conv) {
			right = ((Conv) right).getOp();
		}

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

		if (right instanceof Conv) {
			right = ((Conv) right).getOp();
		}

		// reduce x = y / 1 if possible
		if (isConstant(right) && getTargetValue(right).isOne()) {
			addReplacement(FirmUtils.getFirstSuccessor(division), division.getLeft());
		}
	}

	@Override
	public void visit(Mul multiplication) {
		Node left = multiplication.getLeft();
		Node right = multiplication.getRight();
		TargetValue leftTarget = getTargetValue(left);
		TargetValue rightTarget = getTargetValue(right);

		if (left instanceof Conv) {
			left = ((Conv) left).getOp();
		}
		if (right instanceof Conv) {
			right = ((Conv) right).getOp();
		}

		// reduce x = y * 1 if possible
		if (isConstant(left) && leftTarget.isOne()) {
			addReplacement(multiplication, multiplication.getRight());
		} else if (isConstant(right) && rightTarget.isOne()) {
			addReplacement(multiplication, multiplication.getLeft());
		} else if (isConstant(left) && (leftTarget.asInt() & (leftTarget.asInt() - 1)) == 0 && !leftTarget.isNull()) {
			// left side is divisible by power of 2
			Node constant = multiplication.getGraph().newConst(new TargetValue(Integer.numberOfTrailingZeros(leftTarget.asInt()), Mode.getIu()));
			Node shl = multiplication.getGraph().newShl(multiplication.getBlock(), multiplication.getRight(), constant, multiplication.getMode());
			addReplacement(multiplication, shl);
		} else if (rightTarget.isConstant() && (rightTarget.asInt() & (rightTarget.asInt() - 1)) == 0 && !rightTarget.isNull()) {
			// right side is divisible by power of 2
			Node constant = multiplication.getGraph().newConst(new TargetValue(Integer.numberOfTrailingZeros(rightTarget.asInt()), Mode.getIu()));
			Node shl = multiplication.getGraph().newShl(multiplication.getBlock(), multiplication.getLeft(), constant, multiplication.getMode());

			addReplacement(multiplication, shl);
		}

	}

	@Override
	public void visit(Sub sub) {
		Node right = sub.getRight();
		Node left = sub.getLeft();

		if (left instanceof Conv) {
			left = ((Conv) left).getOp();
		}
		if (right instanceof Conv) {
			right = ((Conv) right).getOp();
		}

		// reduce x = y - 0 if possible
		if (isConstant(right) && getTargetValue(right).isNull()) {
			addReplacement(sub, sub.getLeft());
		} else if (getTargetValue(left).isNull()) {
			// replace x = 0 - y with x = -y
			Node minus = sub.getGraph().newMinus(sub.getBlock(), sub.getRight(), sub.getRight().getMode());
			addReplacement(sub, minus);
		} else if (isConstant(right)) {
			// replace y = x - constant with y = - constant + x
			Const rightConst = (Const) right;
			int value = 0 - rightConst.getTarval().asInt();
			Node constant = right.getGraph().newConst(value, sub.getMode());
			Node add = sub.getGraph().newAdd(sub.getBlock(), constant, left, sub.getMode());
			addReplacement(sub, add);
		}
	}

}
