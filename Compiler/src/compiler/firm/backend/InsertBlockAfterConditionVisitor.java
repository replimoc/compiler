package compiler.firm.backend;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.nodes.Cond;
import firm.nodes.Node;

public class InsertBlockAfterConditionVisitor extends AbstractFirmNodesVisitor {

	@Override
	public void visit(Cond condition) {
		for (Edge edge : BackEdges.getOuts(condition)) {
			createBlockForSuccessor(edge.node);
		}
	}

	private void createBlockForSuccessor(Node node) {
		Graph graph = node.getGraph();
		Node oldBlock = FirmUtils.getFirstSuccessor(node);

		Node newBlock = graph.newBlock(new Node[] { node });
		Node newJmp = graph.newJmp(newBlock);

		for (int i = 0; i < oldBlock.getPredCount(); i++) {
			if (oldBlock.getPred(i).equals(node))
				oldBlock.setPred(i, newJmp);
		}
	}
}
