package compiler.firm.optimization.visitor;

import java.util.HashMap;

import firm.nodes.Add;
import firm.nodes.Const;
import firm.nodes.Conv;
import firm.nodes.Div;
import firm.nodes.Mod;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Sub;

public class CommonSubexpressionEliminationVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new CommonSubexpressionEliminationVisitor();
		}
	};

	private HashMap<NodeValue, Node> nodeValues = new HashMap<>();

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	/**
	 * Get the value of the given node.
	 * 
	 * @param node
	 * @return
	 */
	private int getValue(Node node) {
		if (node instanceof Const) {
			return ((Const) node).getTarval().asInt();
		} else if (node instanceof Conv) {
			return getValue(node.getPred(0));
		} else {
			if (nodeReplacements.containsKey(node)) {
				return getValue(nodeReplacements.get(node));
			} else {
				return node.getNr();
			}
		}
	}

	/**
	 * Check if the given nodes have the same type and that their predecessors have the same type.
	 * 
	 * @param originNode
	 * @param targetNode
	 * @return
	 */
	private boolean sameType(Node originNode, Node targetNode) {
		if (!originNode.getClass().equals(targetNode.getClass()))
			return false;
		if (originNode.getPredCount() != targetNode.getPredCount())
			return false;
		L1: for (Node n1 : originNode.getPreds()) {
			for (Node n2 : targetNode.getPreds()) {
				if (n1.getClass().equals(n2.getClass())) {
					continue L1;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Transfer function for a node with 2 predecessors.
	 * 
	 * @param node
	 * @param left
	 * @param right
	 */
	private void binaryTransferFunction(Node node, int left, int right) {
		Node target = nodeValues.get(new NodeValue(left, right));
		if (target != null) {
			if (!sameType(target, node)) {
				return;
			}
			if (!node.equals(target) && !nodeReplacements.containsKey(target) && !nodeReplacements.containsKey(node)
					&& target.getBlock().equals(node.getBlock())) {
				addReplacement(node, target);
			}
		} else {
			nodeValues.put(new NodeValue(left, right), node);
		}
	}

	@Override
	public void visit(Conv conv) {
		Node pred = conv.getPred(0);
		nodeValues.put(new NodeValue(getValue(pred)), conv);
	}

	@Override
	public void visit(Const cons) {
		nodeValues.put(new NodeValue(getValue(cons)), cons);
	}

	@Override
	public void visit(Proj proj) {
		nodeValues.put(new NodeValue(getValue(proj)), proj);
	}

	@Override
	public void visit(Add add) {
		Node leftNode = nodeReplacements.get(add.getLeft()) == null ? add.getLeft() : nodeReplacements.get(add.getLeft());
		Node rightNode = nodeReplacements.get(add.getRight()) == null ? add.getRight() : nodeReplacements.get(add.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(add, left, right);
	}

	@Override
	public void visit(Sub sub) {
		Node leftNode = nodeReplacements.get(sub.getLeft()) == null ? sub.getLeft() : nodeReplacements.get(sub.getLeft());
		Node rightNode = nodeReplacements.get(sub.getRight()) == null ? sub.getRight() : nodeReplacements.get(sub.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(sub, left, right);
	}

	@Override
	public void visit(Div div) {
		Node leftNode = nodeReplacements.get(div.getLeft()) == null ? div.getLeft() : nodeReplacements.get(div.getLeft());
		Node rightNode = nodeReplacements.get(div.getRight()) == null ? div.getRight() : nodeReplacements.get(div.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(div, left, right);
	}

	@Override
	public void visit(Mod mod) {
		Node leftNode = nodeReplacements.get(mod.getLeft()) == null ? mod.getLeft() : nodeReplacements.get(mod.getLeft());
		Node rightNode = nodeReplacements.get(mod.getRight()) == null ? mod.getRight() : nodeReplacements.get(mod.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(mod, left, right);
	}

	@Override
	public void visit(Shl shl) {
		Node leftNode = nodeReplacements.get(shl.getLeft()) == null ? shl.getLeft() : nodeReplacements.get(shl.getLeft());
		Node rightNode = nodeReplacements.get(shl.getRight()) == null ? shl.getRight() : nodeReplacements.get(shl.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(shl, left, right);
	}

	@Override
	public void visit(Shr shr) {
		Node leftNode = nodeReplacements.get(shr.getLeft()) == null ? shr.getLeft() : nodeReplacements.get(shr.getLeft());
		Node rightNode = nodeReplacements.get(shr.getRight()) == null ? shr.getRight() : nodeReplacements.get(shr.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(shr, left, right);
	}

	@Override
	public void visit(Mul mul) {
		Node leftNode = nodeReplacements.get(mul.getLeft()) == null ? mul.getLeft() : nodeReplacements.get(mul.getLeft());
		Node rightNode = nodeReplacements.get(mul.getRight()) == null ? mul.getRight() : nodeReplacements.get(mul.getRight());
		int left = getValue(leftNode);
		int right = getValue(rightNode);

		binaryTransferFunction(mul, left, right);
	}

	@Override
	public void visit(Phi phi) {
		if (phi.getPredCount() == 2) {
			Node leftNode = nodeReplacements.get(phi.getPred(0)) == null ? phi.getPred(0) : nodeReplacements.get(phi.getPred(0));
			Node rightNode = nodeReplacements.get(phi.getPred(1)) == null ? phi.getPred(1) : nodeReplacements.get(phi.getPred(1));
			int left = getValue(leftNode);
			int right = getValue(rightNode);

			binaryTransferFunction(phi, left, right);
		} else {
			nodeValues.put(new NodeValue(getValue(phi)), phi);
		}
	}

	private class NodeValue {
		final int leftPred;
		final int rightPred;

		private NodeValue(int left) {
			this.leftPred = left;
			this.rightPred = -1;
		}

		private NodeValue(int left, int right) {
			this.leftPred = left;
			this.rightPred = right;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + leftPred;
			result = prime * result + rightPred;
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
			NodeValue other = (NodeValue) obj;

			if (leftPred == other.leftPred && rightPred == other.rightPred)
				return true;
			if (leftPred == other.rightPred && rightPred == other.leftPred)
				return true;
			return false;
		}

	}
}
