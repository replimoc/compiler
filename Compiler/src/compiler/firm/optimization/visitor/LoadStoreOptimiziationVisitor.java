package compiler.firm.optimization.visitor;

import java.util.HashMap;

import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Store;

public class LoadStoreOptimiziationVisitor extends OptimizationVisitor<Node> {

	public LoadStoreOptimiziationVisitor(ProgramDetails programDetails) {
		this.programDetails = programDetails;
	}

	public static final OptimizationVisitorFactory<Node> FACTORY(final ProgramDetails programDetails) {
		return new OptimizationVisitorFactory<Node>() {
			@Override
			public OptimizationVisitor<Node> create() {
				return new LoadStoreOptimiziationVisitor(programDetails);
			}
		};
	}

	private final ProgramDetails programDetails;

	private Load lastLoadNode;
	private Store lastStoreNode;

	private boolean isSideEffectFree(Call node) {
		final Address address = (Address) node.getPred(1);
		Entity calledEntity = address.getEntity();
		EntityDetails calledEntityDetails = programDetails.getEntityDetails(calledEntity);

		return calledEntityDetails.hasSideEffects() == false;
	}

	private boolean isMemProj(Node node) {
		return node instanceof Proj && node.getMode().equals(Mode.getM());
	}

	private Node getMemProjPred(Node node) {
		Node memProj = null;
		for (int i = 0; i < node.getPredCount(); i++) {
			Node pred = node.getPred(i);
			if (isMemProj(pred)) {
				if (memProj == null) {
					memProj = pred;
				} else {
					memProj = null; // TODO: Case where we have two mem proj predecessors.
				}
			}
		}

		if (memProj.getPredCount() == 1) {
			return memProj.getPred(0);
		} else {
			return null;
		}
	}

	private Node getFirstMemoryPredecessor(Node node) {
		// System.out.println("getFirstMemoryPredecessor " + node);
		for (int i = 0; i < node.getPredCount(); i++) {
			if (node.getPred(i).getMode().equals(Mode.getM())) {
				return node.getPred(i);
			} else if (node.getPred(i) instanceof Load || node.getPred(i) instanceof Store) {
				return node.getPred(i);
			}
			// else if (node.getPred(i) instanceof Call) {
			// return node.getPred(i);
			// }
		}
		return null;
	}

	private boolean existSavePathToLastMemNode(Node node, Node memoryNode) {
		if (node == null || memoryNode == null) {
			return false;
		}

		if (node.equals(memoryNode)) {
			return false;
		}

		if (node.getBlock().equals(memoryNode.getBlock()) == false) {
			return false;
		}

		Node currentNode = getFirstMemoryPredecessor(node);

		while (currentNode != null && currentNode.equals(memoryNode) == false) {
			if (currentNode.getBlock().equals(memoryNode.getBlock()) == false) { // nodes are not in the same block
				return false;
			} else if (currentNode instanceof Phi) { // phi -> no opt possible
				return false;
			} else if (currentNode instanceof Call && isSideEffectFree((Call) currentNode) == false) { // allow only call that are side effect free
				return false;
			} else if (currentNode instanceof Store) { // if store then no opt possible
				// if (sameMemoryAdress(node, currentNode)) {
				// return false;
				// }
				return false;
			}

			currentNode = getFirstMemoryPredecessor(currentNode);
		}

		if (currentNode == null) {
			return false;
		}

		return true;
	}

	private boolean sameMemoryAdress(Node node1, Node node2) {
		// check if the two memory nodes are dependent on equal nodes

		// if unequal number of nodes, then quit
		if (node1.getPredCount() < 2 || node2.getPredCount() < 2) {
			return false;
		}

		// first node is memory

		// second successor is memory adress
		if (node1.getPred(1).equals(node2.getPred(1)) == false) {
			return false;
		}

		// System.out.println("sameMemoryAdress:" + node1.getPred(1) + ":" + node2.getPred(1));

		// third is value

		return true;
	}

	private boolean isAdressInSameBlock(Node node1, Node node2) {
		// check if the two memory nodes are dependent on equal nodes

		// if unequal number of nodes, then quit
		if (node1.getPredCount() < 2 || node2.getPredCount() < 2) {
			return false;
		}

		// first node is memory

		// second successor is memory adress
		if (node1.getPred(1).equals(node2.getPred(1)) == false) {
			return false;
		}

		if (node1.getPred(1).getBlock().equals(node1.getBlock()) == false || node2.getPred(1).getBlock().equals(node2.getBlock()) == false) {
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
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	@Override
	public void visit(Load load) {
		// load - load optimization
		if (existSavePathToLastMemNode(load, lastLoadNode) && sameMemoryAdress(load, lastLoadNode) && isAdressInSameBlock(load, lastLoadNode)) {
			// System.out.println(getMethodName(load) + ":" + load + " to " + lastLodeNode + " optimiziation");

			for (Edge edge : BackEdges.getOuts(load)) {
				if (isMemProj(edge.node)) {
					Node memProjPred = getMemProjPred(load);
					edge.node.setPred(edge.pos, memProjPred);
				} else {
					edge.node.setPred(edge.pos, lastLoadNode);
				}
			}

			uniteProjSuccessors(lastLoadNode);

			addReplacement(load, load.getGraph().newBad(load.getMode()));
		}
		// store - load optimization
		else if (existSavePathToLastMemNode(load, lastStoreNode) && sameMemoryAdress(load, lastStoreNode) && isAdressInSameBlock(load, lastStoreNode)) {
			// System.out.println(getMethodName(load) + ":" + load + " to " + lastStoreNode + " optimiziation");

			for (Edge edge : BackEdges.getOuts(load)) {
				if (isMemProj(edge.node)) {
					Node memProjPred = getMemProjPred(load);
					// System.out.println(edge.node + " > " + memProjPred);
					edge.node.setPred(edge.pos, memProjPred);
				} else {
					addReplacement(edge.node, edge.node.getGraph().newBad(edge.node.getMode()));
					for (Edge projEdges : BackEdges.getOuts(edge.node)) {
						projEdges.node.setPred(projEdges.pos, lastStoreNode.getPred(2));
						// System.out.println(projEdges.node + " > " + lastStoreNode.getPred(2));
					}
				}
			}

			addReplacement(load, load.getGraph().newBad(load.getMode()));
		}

		lastLoadNode = load;
	}

	@Override
	public void visit(Store store) {
		// // store - store optimization
		if (existSavePathToLastMemNode(store, lastStoreNode) && sameMemoryAdress(store, lastStoreNode)) {
			// System.out.println(getMethodName(store) + ":" + store + " to " + lastStoreNode);

			store.setPred(0, lastStoreNode.getPred(0));

			// addReplacement(lastStoreNode, lastStoreNode.getGraph().newBad(lastStoreNode.getMode()));
		}

		lastStoreNode = store;
	}
}
