package compiler.firm.optimization.visitor;

import java.util.HashMap;

import compiler.firm.FirmUtils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Proj;

public class LoadStoreOptimiziationVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LoadStoreOptimiziationVisitor();
		}
	};

	private Load lastLodeNode;

	private boolean existSavePathToLastLoad(Load node) {
		if (lastLodeNode != null && // if last node != null
				node.getBlock().equals(lastLodeNode.getBlock()) && // both loads in the same block
				node.getPred(0) instanceof Proj) {
			Proj proj = (Proj) node.getPred(0);
			if (proj.getPred() instanceof Load && proj.getPred().equals(lastLodeNode) &&
					node.getPred(1).equals(lastLodeNode.getPred(1))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	@Override
	public void visit(Load load) {
		if (existSavePathToLastLoad(load)) {
			// System.out.println(load + " is being optimized!");

			boolean firstSuc = true;
			for (Edge edge : BackEdges.getOuts(load)) {
				edge.node.setPred(edge.pos, lastLodeNode);

				if (firstSuc == false) {
					Node node = FirmUtils.getFirstSuccessor(edge.node);
					node.setPred(0, edge.node);
				}

				firstSuc = false;
			}

			Graph.killNode(load);
		}

		lastLodeNode = load;
	}
}
