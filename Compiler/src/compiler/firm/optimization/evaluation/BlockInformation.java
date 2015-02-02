package compiler.firm.optimization.evaluation;

import firm.nodes.Node;

public class BlockInformation {
	private final Node endNode;

	public BlockInformation(Node endNode) {
		this.endNode = endNode;
	}

	public Node getEndNode() {
		return endNode;
	}
}
