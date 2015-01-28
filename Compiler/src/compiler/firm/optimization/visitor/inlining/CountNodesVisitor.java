package compiler.firm.optimization.visitor.inlining;

import firm.nodes.Call;
import firm.nodes.Node;

public class CountNodesVisitor extends VisitAllNodeVisitor {

	private int numNodes = 0;

	public int getNumNodes() {
		return numNodes;
	}

	@Override
	protected Node visitNode(Node node) {
		numNodes++;
		return null;
	}

	@Override
	public void visit(Call call) {
		numNodes += 10000; // Do not inline recursions
	}

}
