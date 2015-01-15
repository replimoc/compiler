package compiler.firm.optimization.visitor;

import java.util.HashMap;

import firm.nodes.Add;
import firm.nodes.Mul;
import firm.nodes.Node;

public class NormalizationVisitor extends OptimizationVisitor<Node> {
	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new NormalizationVisitor();
		}
	};

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	@Override
	public void visit(Add add) {
		Node left = add.getLeft();
		Node right = add.getRight();

		// normalize y = x + const
		// to y = const + x
		if (!isConstant(left) && isConstant(right)) {
			Node newAdd = add.getGraph().newAdd(add.getBlock(), right, left, add.getMode());
			addReplacement(add, newAdd);
		}
	}

	@Override
	public void visit(Mul mul) {
		Node left = mul.getLeft();
		Node right = mul.getRight();

		// normalize y = x * const
		// to y = const * x
		if (!isConstant(left) && isConstant(right)) {
			Node newMul = mul.getGraph().newMul(mul.getBlock(), right, left, mul.getMode());
			addReplacement(mul, newMul);
		}

	}

}
