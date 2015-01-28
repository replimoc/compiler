package compiler.firm.optimization.visitor.inlining;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.nodes.Call;
import firm.nodes.Node;

public class CountNodesVisitor extends AbstractFirmNodesVisitor {

	private int numNodes = 0;

	public int getNumNodes() {
		return numNodes;
	}

	@Override
	protected void visitNode(Node node) {
		numNodes++;
	}

	@Override
	public void visit(Call call) {
		numNodes += 10000; // Do not inline recursions
	}

}
