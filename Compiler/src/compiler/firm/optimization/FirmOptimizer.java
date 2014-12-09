package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import firm.Graph;
import firm.Program;
import firm.TargetValue;
import firm.nodes.Node;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor visitor = new OptimizationVisitor(workList);
			graph.walkTopological(visitor);

			workList(workList, visitor);

			replaceNodesWithTargets(graph, visitor.getTargetValues());
		}
	}

	private static void workList(LinkedList<Node> workList, OptimizationVisitor visitor) {
		while (!workList.isEmpty()) {
			Node node = workList.pop();
			node.accept(visitor);
		}
	}

	private static void replaceNodesWithTargets(Graph graph, HashMap<Node, TargetValue> targetValuesMap) {
		for (Entry<Node, TargetValue> targetEntry : targetValuesMap.entrySet()) {
			Node node = targetEntry.getKey();
			TargetValue targetValue = targetEntry.getValue();

			if (node.getPredCount() > 0) {
				if (targetValue.isConstant()) {
					Graph.exchange(node, graph.newConst(targetValue));
				}
			}
		}
	}
}
