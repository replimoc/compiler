package compiler.firm.optimization.visitor;

import java.util.HashMap;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
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

	private String getMethodName(Node node) {
		return node.getGraph().getEntity().getLdName();
	}

	private void uniteProjSuccessors(Load load) {
		Proj memProj = null;
		Proj nonMemProj = null;

		for (Edge edge : BackEdges.getOuts(load)) {
			if (edge.node.getMode().equals(Mode.getM())) {
				if (memProj == null) {
					memProj = (Proj) edge.node;
				} else {
					for (Edge loadSuccEdge : BackEdges.getOuts(edge.node)) {
						loadSuccEdge.node.setPred(loadSuccEdge.pos, memProj);
					}
					addReplacement(edge.node, edge.node.getGraph().newBad(edge.node.getMode()));
				}
			} else if (edge.node.getMode().equals(Mode.getM()) == false) {
				if (nonMemProj == null) {
					nonMemProj = (Proj) edge.node;
				} else {
					for (Edge loadSuccEdge : BackEdges.getOuts(edge.node)) {
						loadSuccEdge.node.setPred(loadSuccEdge.pos, nonMemProj);
					}
					addReplacement(edge.node, edge.node.getGraph().newBad(edge.node.getMode()));
				}
			}
		}

		System.out.println(load + " united (" + BackEdges.getNOuts(load) + ")");
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	@Override
	public void visit(Load load) {
		if (existSavePathToLastLoad(load)) {
			// System.out.println(load + " is being optimized!");
			// System.out.println(getMethodName(load) + ":" + load);

			for (Edge edge : BackEdges.getOuts(load)) {
				edge.node.setPred(edge.pos, lastLodeNode);
			}

			uniteProjSuccessors(lastLodeNode);

			addReplacement(load, load.getGraph().newBad(load.getMode()));
		}

		lastLodeNode = load;
	}
}
