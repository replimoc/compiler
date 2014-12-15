package compiler.firm.optimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.GraphBase;
import firm.Program;
import firm.bindings.binding_irgopt;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.Phi;

public final class FirmOptimizer {
	private FirmOptimizer() {
	}

	public static void optimize() {
		boolean finished = true;
		do {
			finished = true;
			finished &= optimize(ConstantFoldingVisitor.class);
			finished &= optimize(ArithmeticVisitor.class);
			finished &= optimize(ControlFlowVisitor.class);
		} while (!finished);
	}

	public static <T extends OptimizationVisitor> boolean optimize(Class<T> visitorClass) {
		boolean finished = true;
		for (Graph graph : Program.getGraphs()) {
			LinkedList<Node> workList = new LinkedList<>();

			try {
				T visitor = visitorClass.newInstance();

				BackEdges.enable(graph);
				walkTopological(graph, workList, visitor);
				workList(workList, visitor);
				BackEdges.disable(graph);

				HashMap<Node, Target> targetValues = visitor.getTargetValues();

				finished &= targetValues.isEmpty();

				replaceNodesWithTargets(graph, targetValues);

				targetValues.clear();

				binding_irgopt.remove_unreachable_code(graph.ptr);
				binding_irgopt.remove_bads(graph.ptr);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return finished;
	}

	private static void walkTopological(Graph graph, LinkedList<Node> workList, OptimizationVisitor visitor) {
		walkTopological(graph.getEnd(), workList, visitor);
	}

	/**
	 * Algorithm taken from {@link GraphBase}.walkTopological() and adapted by @author Andreas Eberle
	 * 
	 * @param node
	 * @param visitor
	 */
	private static void walkTopological(Node node, LinkedList<Node> workList, OptimizationVisitor visitor) {
		if (node.visited())
			return;

		/* only break loops at phi/block nodes */
		boolean isLoopBreaker = node.getClass() == Phi.class || node.getClass() == Block.class;
		if (isLoopBreaker) {
			node.markVisited();
		}

		if (node.getBlock() != null) {
			walkTopological(node.getBlock(), workList, visitor);
		}
		for (Node pred : node.getPreds()) {
			walkTopological(pred, workList, visitor);
		}

		if (isLoopBreaker || !node.visited()) {
			visitNode(node, workList, visitor);
		}
		node.markVisited();
	}

	private static void visitNode(Node node, LinkedList<Node> workList, OptimizationVisitor visitor) {
		HashMap<Node, Target> targetValues = visitor.getTargetValues();
		Target oldTarget = targetValues.get(node);
		node.accept(visitor);
		Target newTarget = targetValues.get(node);

		if (oldTarget == null || !oldTarget.equals(newTarget)) {
			for (Edge e : BackEdges.getOuts(node)) {
				workList.push(e.node);
			}
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
				if (target.isNode()) {
					Graph.exchange(node, target.getNode());
				} else {
					if (target.isFixpointReached() && target.getTargetValue().isConstant()) {
						Graph.exchange(node, graph.newConst(target.getTargetValue()));
					}
				}
			}
		}
	}
}
