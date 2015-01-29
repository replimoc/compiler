package compiler.firm.optimization.visitor;

import java.util.HashMap;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Proj;
import firm.nodes.Store;

public class LoadStoreOptimiziationVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LoadStoreOptimiziationVisitor();
		}
	};

	private Load lastLodeNode;
	private Store lastStoreNode;

	private boolean existSavePathToLastMemNode(Node node, Node memoryNode) {
		if (memoryNode != null && // if last node != null
				node.getBlock().equals(memoryNode.getBlock()) && // both loads in the same block
				node.getPred(0) instanceof Proj) {
			Proj proj = (Proj) node.getPred(0);
			if ((proj.getPred() instanceof Load || proj.getPred() instanceof Store) && proj.getPred().equals(memoryNode)) {
				return true;
			}
		}
		return false;
	}

	private boolean dependOnSameNode(Node node1, Node node2) {
		// check if the two memory nodes are dependent on equal nodes

		// if unequal number of nodes, then quit
		if (node1.getPredCount() != node2.getPredCount()) {
			return false;
		}

		// first node is memory

		// second successor is memory adress
		if (node1.getPred(1).equals(node2.getPred(1)) == false) {
			return false;
		}

		// third is value

		return true;
	}

	private String getMethodName(Node node) {
		return node.getGraph().getEntity().getLdName();
	}

	private void uniteProjSuccessors(Node node) {
		Proj memProj = null;
		Proj nonMemProj = null;

		for (Edge edge : BackEdges.getOuts(node)) {
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

		// System.out.println(node + " united (" + BackEdges.getNOuts(node) + ")");
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	@Override
	public void visit(Load load) {
		if (existSavePathToLastMemNode(load, lastLodeNode) && dependOnSameNode(load, lastLodeNode)) {
			// System.out.println(getMethodName(load) + ":" + load);

			for (Edge edge : BackEdges.getOuts(load)) {
				edge.node.setPred(edge.pos, lastLodeNode);
			}

			uniteProjSuccessors(lastLodeNode);

			addReplacement(load, load.getGraph().newBad(load.getMode()));
		}

		lastLodeNode = load;
	}

	@Override
	public void visit(Store store) {
		if (existSavePathToLastMemNode(store, lastStoreNode)) {
			// System.out.println("safe path");
			if (dependOnSameNode(store, lastStoreNode)) {
				System.out.println(getMethodName(store) + ":" + store);

				store.setPred(0, lastStoreNode.getPred(0));

				// uniteProjSuccessors(lastStoreNode);

				addReplacement(lastStoreNode, lastStoreNode.getGraph().newBad(lastStoreNode.getMode()));
			}
		}

		lastStoreNode = store;
	}
}
