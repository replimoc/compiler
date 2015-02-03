package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.utils.Pair;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.nodes.Cond;
import firm.nodes.Node;
import firm.nodes.Proj;

public class LoopFusionVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LoopFusionVisitor();
		}
	};

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return new HashMap<>();
	}

	@Override
	public void visit(Cond condition) {
		OptimizationUtils optimizationUtils = new OptimizationUtils(condition.getGraph());
		HashMap<Node, Node> backedges = optimizationUtils.getBackEdges();
		Node block = condition.getBlock();
		if (backedges.containsValue(block)) {
			Pair<Node, Proj> continueInfo = getLoopContinueBlocks(condition);

			if (backedges.containsValue(continueInfo.first)) { // Next is also a loop
				Set<Node> nodes = optimizationUtils.getBlockNodes().get(continueInfo.first);
				Cond condition2 = getConditionNode(nodes);

				System.out.println(condition);

			}
		}
	}

	private Cond getConditionNode(Set<Node> nodes) {
		Cond cond = null;
		for (Node node : nodes) {
			if (node instanceof Cond)
				cond = (Cond) node;
		}
		return cond;
	}

	private Pair<Node, Proj> getLoopContinueBlocks(Node condition) {
		Node continueBlock = null;
		Proj continueProj = null;
		for (Edge projEdge : BackEdges.getOuts(condition)) {
			Proj proj = (Proj) projEdge.node;
			Node successorBlock = FirmUtils.getFirstSuccessor(proj);

			if (!FirmUtils.blockPostdominates(condition.getBlock(), successorBlock)) {
				continueBlock = successorBlock;
				continueProj = proj;
			}
		}
		return new Pair<>(continueBlock, continueProj);
	}
}
