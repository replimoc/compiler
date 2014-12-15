package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import firm.BackEdges;
import firm.Graph;
import firm.Program;
import firm.bindings.binding_irgopt;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.Phi;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor visitor = new OptimizationVisitor(workList);

			BackEdges.enable(graph);
			walkTopological(graph, visitor);
			workList(workList, visitor);
			BackEdges.disable(graph);

			replaceNodesWithTargets(graph, visitor.getTargetValues());

			binding_irgopt.remove_unreachable_code(graph.ptr);
			binding_irgopt.remove_bads(graph.ptr);
		}
	}

	private static void walkTopological(Graph graph, OptimizationVisitor visitor) {
		walkTopological(graph.getEnd(), visitor);
	}

	private static void walkTopological(Node node, OptimizationVisitor visitor) {
		if (node.visited())
			return;

		/* only break loops at phi/block nodes */
		boolean isLoopBreaker = node.getClass() == Phi.class || node.getClass() == Block.class;
		if (isLoopBreaker) {
			node.markVisited();
		}

		if (node.getBlock() != null) {
			walkTopological(node.getBlock(), visitor);
		}
		for (Node pred : node.getPreds()) {
			walkTopological(pred, visitor);
		}

		if (isLoopBreaker || !node.visited()) {
			visitNode(node, visitor);
		}
		node.markVisited();
	}

	private static void visitNode(Node node, OptimizationVisitor visitor) {
		node.accept(visitor);
	}

	private static void workList(LinkedList<Node> workList, OptimizationVisitor visitor) {
		while (!workList.isEmpty()) {
			Node node = workList.pop();
			node.accept(visitor);
		}
	}

	private static void replaceNodesWithTargets(Graph graph, HashMap<Node, Target> targetValuesMap) {
		for (Entry<Node, Target> targetEntry : targetValuesMap.entrySet()) {
			Node node = targetEntry.getKey();
			Target target = targetEntry.getValue();

			if (node.getPredCount() > 0) {
				if (target.isNode()) {
					Graph.exchange(node, target.getNode());
				} else {
					if (target.isConstant() && target.getTargetValue().isConstant()) {
						Graph.exchange(node, graph.newConst(target.getTargetValue()));
					}
				}
			}
		}
	}
}
