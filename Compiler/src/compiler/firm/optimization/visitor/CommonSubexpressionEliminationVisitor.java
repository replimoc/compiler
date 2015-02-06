package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.nodes.Add;
import firm.nodes.Block;
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
import firm.nodes.Start;
import firm.nodes.Sub;

public class CommonSubexpressionEliminationVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new CommonSubexpressionEliminationVisitor();
		}
	};

	private final HashMap<NodeValue, Node> nodeValues = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();

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
	private long getValue(Node node) {
		if (node instanceof Const) {
			return ((Const) node).getTarval().asLong();
		} else if (node instanceof Conv) {
			return getValue(node.getPred(0));
		} else if (nodeReplacements.containsKey(node)) {
			return getValue(nodeReplacements.get(node));
		} else {
			return node.getNr();
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
			for (int i = 0; i < targetNode.getPredCount(); i++) {
				if (n1.getClass().equals(targetNode.getPred(i).getClass())) {
					// match for n1 found
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
	private void binaryTransferFunction(Node node, long left, long right) {
		if (dominators.size() == 0) {
			node.getGraph().getStart().accept(this);
		}
		Node target = nodeValues.get(new NodeValue(left, right));
		if (target != null) {
			if (!sameType(target, node)) {
				return;
			}
			if (!node.equals(target) && !nodeReplacements.containsKey(target) && !nodeReplacements.containsKey(node)
					&& dominators.get(node.getBlock()).contains(target.getBlock())) {
				addReplacement(node, target);
			} else if (!node.equals(target) && !nodeReplacements.containsKey(target) && !nodeReplacements.containsKey(node) &&
					dominators.get(target.getBlock()).contains(node.getBlock())) {
				addReplacement(target, node);
			} else {
				return;
			}
		} else {
			nodeValues.put(new NodeValue(left, right), node);
		}
	}

	/**
	 * Transfer function for a mod or div node with 2 predecessors.
	 * 
	 * @param node
	 * @param left
	 * @param right
	 */
	private void modDivTransferFunction(Node node, long left, long right) {
		if (dominators.size() == 0) {
			node.getGraph().getStart().accept(this);
		}
		Node target = nodeValues.get(new NodeValue(left, right));
		if (target != null) {
			if (!sameType(target, node)) {
				return;
			}
			if (!node.equals(target) && !nodeReplacements.containsKey(target) && !nodeReplacements.containsKey(node)) {
				List<Node> suc = new ArrayList<Node>();
				List<Node> sucTarget = new ArrayList<Node>();
				for (Edge e : BackEdges.getOuts(node)) {
					suc.add(e.node);
				}
				for (Edge e : BackEdges.getOuts(target)) {
					sucTarget.add(e.node);
				}
				if (suc.size() == 0 || sucTarget.size() == 0)
					return;

				// replace the Proj node so the firm backend is happy
				if (node.getBlock().equals(target.getBlock())) {
					if (isReachable(node, target)) {
						replaceDivMod(target, node, sucTarget, suc);
					} else {
						replaceDivMod(node, target, suc, sucTarget);
					}
				} else if (dominators.get(node.getBlock()).contains(target.getBlock())) {
					addReplacement(node, target);
					addReplacement(suc.get(0), sucTarget.get(0));
					// replaceDivMod(node, target, suc, sucTarget);
				} else if (dominators.get(target.getBlock()).contains(node.getBlock())) {
					addReplacement(target, node);
					addReplacement(sucTarget.get(0), suc.get(0));
					// replaceDivMod(target, node, sucTarget, suc);
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			nodeValues.put(new NodeValue(left, right), node);
		}
	}

	private void replaceDivMod(Node src, Node dest, List<Node> srcPred, List<Node> destPred) {
		Node first = destPred.get(0);
		for (int i = 1; i < destPred.size(); i++) {
			addReplacement(destPred.get(i), first);
		}
		for (int i = 0; i < srcPred.size(); i++) {
			addReplacement(srcPred.get(i), first);
		}
		addReplacement(src, dest);
	}

	private boolean isReachable(Node src, Node dest) {
		Node tmp = src.getPred(0);
		while (tmp != null && tmp.getBlock().equals(src.getBlock())) {
			if (tmp.equals(dest)) {
				// src is dominated by dest
				return true;
			}
			if (tmp.getPredCount() > 0) {
				tmp = tmp.getPred(0); // get memory predecessor
			} else {
				return false;
			}
		}
		return false;
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
		Node leftReplacement = nodeReplacements.get(add.getLeft());
		Node leftNode = leftReplacement == null ? add.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(add.getRight());
		Node rightNode = rightReplacement == null ? add.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		binaryTransferFunction(add, left, right);
	}

	@Override
	public void visit(Sub sub) {
		Node leftReplacement = nodeReplacements.get(sub.getLeft());
		Node leftNode = leftReplacement == null ? sub.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(sub.getRight());
		Node rightNode = rightReplacement == null ? sub.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		binaryTransferFunction(sub, left, right);
	}

	@Override
	public void visit(Div div) {
		Node leftReplacement = nodeReplacements.get(div.getLeft());
		Node leftNode = leftReplacement == null ? div.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(div.getRight());
		Node rightNode = rightReplacement == null ? div.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		modDivTransferFunction(div, left, right);
	}

	@Override
	public void visit(Mod mod) {
		Node leftReplacement = nodeReplacements.get(mod.getLeft());
		Node leftNode = leftReplacement == null ? mod.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(mod.getRight());
		Node rightNode = rightReplacement == null ? mod.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		modDivTransferFunction(mod, left, right);
	}

	@Override
	public void visit(Shl shl) {
		Node leftReplacement = nodeReplacements.get(shl.getLeft());
		Node leftNode = leftReplacement == null ? shl.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(shl.getRight());
		Node rightNode = rightReplacement == null ? shl.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		binaryTransferFunction(shl, left, right);
	}

	@Override
	public void visit(Shr shr) {
		Node leftReplacement = nodeReplacements.get(shr.getLeft());
		Node leftNode = leftReplacement == null ? shr.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(shr.getRight());
		Node rightNode = rightReplacement == null ? shr.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		binaryTransferFunction(shr, left, right);
	}

	@Override
	public void visit(Mul mul) {
		Node leftReplacement = nodeReplacements.get(mul.getLeft());
		Node leftNode = leftReplacement == null ? mul.getLeft() : leftReplacement;
		Node rightReplacement = nodeReplacements.get(mul.getRight());
		Node rightNode = rightReplacement == null ? mul.getRight() : rightReplacement;
		long left = getValue(leftNode);
		long right = getValue(rightNode);

		binaryTransferFunction(mul, left, right);
	}

	@Override
	public void visit(Phi phi) {
		nodeValues.put(new NodeValue(getValue(phi)), phi);
	}

	@Override
	public void visit(Start start) {
		OptimizationUtils utils = new OptimizationUtils(start.getGraph());
		dominators = utils.getDominators();
	}

	private class NodeValue {
		final long leftPred;
		final long rightPred;

		private NodeValue(long left) {
			this.leftPred = left;
			this.rightPred = -1;
		}

		private NodeValue(long left, long right) {
			this.leftPred = left;
			this.rightPred = right;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (leftPred ^ (leftPred >>> 32));
			result = prime * result + (int) (rightPred ^ (rightPred >>> 32));
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
