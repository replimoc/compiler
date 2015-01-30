package compiler.firm.optimization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.evaluation.BlockInformation;
import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;
import compiler.firm.optimization.visitor.InliningCopyGraphVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Node;

public final class MethodInliner {

	public static boolean inlineCalls(ProgramDetails programDetails) {
		boolean changes = false;
		Set<Call> calls = programDetails.getCalls();
		for (Call call : calls) {
			Address address = (Address) call.getPred(1);
			Graph graph = address.getEntity().getGraph();

			if (graph != null) {
				EntityDetails entityDetails = programDetails.getEntityDetails(call);

				if (entityDetails.isInlinable() && !programDetails.isRecursion(call)) {
					changes = true;

					BlockInformation blockInformation = entityDetails.getBlockInformation(call.getBlock());
					inline(call, graph, blockInformation);
					updateGraph(call, programDetails);
				}
			}
		}

		return !changes;
	}

	private static void updateGraph(Call call, ProgramDetails programDetails) {
		Set<Graph> graphs = new HashSet<>();
		graphs.add(call.getGraph());
		for (Call subcall : programDetails.getCalls()) {
			graphs.add(subcall.getGraph());
		}

		programDetails.updateGraphs(graphs);
	}

	private static void inline(Call call, Graph graph, BlockInformation blockInformation) {
		BackEdges.enable(call.getGraph());
		Set<Node> moveNodes = getAllSuccessorsInSameBlock(call);
		if (blockInformation != null) {
			moveNodes.addAll(getAllSuccessorsInSameBlock(blockInformation.getEndNode()));
		}

		Node firstSuccessor = null;
		Node secondSuccessor = null;

		for (Edge edge : BackEdges.getOuts(call)) {
			if (edge.node.getMode().equals(Mode.getM())) {
				firstSuccessor = edge.node;
			} else if (edge.node.getMode().equals(Mode.getT())) {
				secondSuccessor = edge.node;
			}
		}
		BackEdges.disable(call.getGraph());

		List<Node> arguments = new ArrayList<>();
		for (int i = 2; i < call.getPredCount(); i++) {
			arguments.add(call.getPred(i));
		}

		InliningCopyGraphVisitor blockCopyWalker = new InliningCopyGraphVisitor(call, arguments);
		graph.walkPostorder(blockCopyWalker);

		blockCopyWalker.cleanupNodes();

		if (secondSuccessor != null) {
			BackEdges.enable(call.getGraph());
			Node oldCallResult = FirmUtils.getFirstSuccessor(secondSuccessor);
			BackEdges.disable(call.getGraph());

			// remove call result
			Graph.exchange(secondSuccessor, FirmUtils.newBad(secondSuccessor));
			// write result to new result
			Graph.exchange(oldCallResult, blockCopyWalker.getResult());
		}

		// replace call with predecessor to keep control flow
		Graph.exchange(call, call.getPred(0));
		Graph.exchange(blockCopyWalker.getStartProjM(), call.getPred(0));
		Graph.exchange(firstSuccessor, blockCopyWalker.getEndProjM());

		Node useBlock = blockCopyWalker.getLastBlock();

		for (Node node : moveNodes) {
			node.setBlock(useBlock);
		}
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
