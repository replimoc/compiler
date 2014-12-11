package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import compiler.firm.optimization.OptimizationVisitor.Target;

import firm.BackEdges;
import firm.Graph;
import firm.Program;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			OptimizationVisitor visitor = new OptimizationVisitor(workList);

			BackEdges.enable(graph);
			graph.walkTopological(visitor);
			workList(workList, visitor);
			BackEdges.disable(graph);

			replaceNodesWithTargets(graph, visitor.getTargetValues());
		}
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
				if (target.isRemove() && target.getTargetValue().isConstant()) {

					// nodes that have a phi node as predecessor must only be removed if the phi nodes will be removed
					boolean remove = true;
					if (node instanceof Proj) {
						// div and mod need one extra indirection
						for (Node pred : node.getPreds()) {
							for (Node pred2 : pred.getPreds()) {
								if (pred2 instanceof Phi) {
									remove = remove && targetValuesMap.get(pred2).isRemove();
								}
							}
						}
					} else
						for (Node pred : node.getPreds()) {
							if (pred instanceof Phi) {
								remove = remove && targetValuesMap.get(pred).isRemove();
							}
						}
					if (remove) {
						Graph.exchange(node, graph.newConst(target.getTargetValue()));
					}
				}
			}
		}
	}
}
