package compiler.firm.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.evaluation.CallInformation;
import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;
import compiler.firm.optimization.visitor.inlining.CountNodesVisitor;
import compiler.firm.optimization.visitor.inlining.GetNodesForBlockVisitor;
import compiler.firm.optimization.visitor.inlining.GraphInliningCopyOperationVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Node;

public final class MethodInliner {

	public static boolean inlineCalls(ProgramDetails programDetails) {
		boolean changes = false;
		for (Entry<Entity, EntityDetails> entityDetailEntry : programDetails.getEntityDetails().entrySet()) {
			for (Entry<Call, CallInformation> callInfo : entityDetailEntry.getValue().getCallsToEntity().entrySet()) {
				Call call = callInfo.getKey();

				Address address = (Address) call.getPred(1);
				Graph graph = address.getEntity().getGraph();

				if (graph != null) {
					CountNodesVisitor countVisitor = new CountNodesVisitor();
					graph.walk(countVisitor);

					if (countVisitor.getNumNodes() <= 10000) {
						changes = true;
						inline(call, graph);
					}
				}
			}
		}

		return !changes;
	}

	private static void inline(Call call, Graph graph) {
		HashMap<Node, Node> nodeReplacements = new HashMap<>();

		BackEdges.enable(call.getGraph());

		GetNodesForBlockVisitor nodesForBlock = new GetNodesForBlockVisitor(call.getBlock());
		call.getGraph().walk(nodesForBlock);

		Set<Node> moveNodes = getAllSuccessorsInSameBlock(call);
		moveNodes.addAll(getAllSuccessorsInSameBlock(nodesForBlock.getEnd()));

		Node firstSuccessor = null;
		Node secondSuccessor = null;

		for (Edge edge : BackEdges.getOuts(call)) {
			if (edge.node.getMode().equals(Mode.getM())) {
				firstSuccessor = edge.node;
			} else if (edge.node.getMode().equals(Mode.getT())) {
				secondSuccessor = edge.node;
			}
		}

		List<Node> arguments = new ArrayList<>();
		for (int i = 2; i < call.getPredCount(); i++) {
			arguments.add(call.getPred(i));
		}

		BackEdges.disable(call.getGraph());

		GraphInliningCopyOperationVisitor blockCopyWalker = new GraphInliningCopyOperationVisitor(call, arguments);
		graph.walkPostorder(blockCopyWalker);

		blockCopyWalker.cleanupNodes();

		BackEdges.enable(call.getGraph());

		if (secondSuccessor != null) {
			Node oldCallResult = FirmUtils.getFirstSuccessor(secondSuccessor);

			// remove call result
			nodeReplacements.put(secondSuccessor, createBadNode(secondSuccessor));
			// write result to new result
			nodeReplacements.put(oldCallResult, blockCopyWalker.getResult());
		}

		// replace call with predecessor to keep control flow
		nodeReplacements.put(call, call.getPred(0));

		nodeReplacements.put(blockCopyWalker.getStartProjM(), call.getPred(0));

		nodeReplacements.put(firstSuccessor, blockCopyWalker.getEndProjM());

		Node useBlock = blockCopyWalker.getLastBlock();

		for (Node node : moveNodes) {
			node.setBlock(useBlock);
		}

		BackEdges.disable(call.getGraph());

		for (Entry<Node, Node> replacement : nodeReplacements.entrySet()) {
			Graph.exchange(replacement.getKey(), replacement.getValue());
		}
	}

	private static Node createBadNode(Node node) {
		return node.getGraph().newBad(node.getMode());
	}

	private static Set<Node> getAllSuccessorsInSameBlock(Node node) {
		Set<Node> nodes = new HashSet<>();
		if (node != null) {
			getAllSuccessorsInBlock(node, node.getBlock(), nodes);
		}
		return nodes;
	}

	private static void getAllSuccessorsInBlock(Node node, Node block, Set<Node> result) {
		result.add(node);
		for (Edge edge : BackEdges.getOuts(node)) {
			Node successor = edge.node;
			if (successor.getBlock() != null && successor.getBlock().equals(block) && !result.contains(successor)) {
				getAllSuccessorsInBlock(successor, block, result);
			}
		}
	}

}
