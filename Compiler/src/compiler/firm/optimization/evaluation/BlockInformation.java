package compiler.firm.optimization.evaluation;

import firm.nodes.Node;

public class BlockInformation {
	private Node firstModeM;
	private Node lastModeM;
	private Node endNode;

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setFirstModeM(Node firstModeM) {
		this.firstModeM = firstModeM;
	}

	public Node getFirstModeM() {
		return firstModeM;
	}

	public void setLastModeM(Node lastModeM) {
		this.lastModeM = lastModeM;
	}

	public Node getLastModeM() {
		return lastModeM;
	}
}
