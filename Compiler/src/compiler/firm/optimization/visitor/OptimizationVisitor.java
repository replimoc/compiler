package compiler.firm.optimization.visitor;

import java.util.HashMap;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.Mode;
import firm.nodes.Const;
import firm.nodes.Node;

public abstract class OptimizationVisitor<T extends Object> extends AbstractFirmNodesVisitor {

	protected HashMap<Node, Node> nodeReplacements = new HashMap<>();

	public void addReplacement(Node source, Node target) {
		nodeReplacements.put(source, target);
	}

	public HashMap<Node, Node> getNodeReplacements() {
		return nodeReplacements;
	}

	public abstract HashMap<Node, T> getLatticeValues();

	protected boolean isConstant(Node node) {
		return node instanceof Const &&
				(node.getMode().equals(Mode.getIs()) || node.getMode().equals(Mode.getBu()) || node.getMode().equals(Mode.getLu()));
	}

}
