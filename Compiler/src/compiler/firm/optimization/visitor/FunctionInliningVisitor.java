package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.visitor.inlining.CorrectBlockVisitor;
import compiler.firm.optimization.visitor.inlining.GraphInliningCopyOperationVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.Mode;
import firm.Program;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Node;

public class FunctionInliningVisitor extends OptimizationVisitor<Node> {

	private Set<Call> inlinedCalls = new HashSet<>();

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new FunctionInliningVisitor();
		}
	};

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	public Node createBadNode(Node node) {
		return node.getGraph().newBad(node.getMode());
	}

	public Node copyNode(Node node) {
		return node.getGraph().copyNode(node);
	}

	@Override
	public void visit(Call call) {
		if (inlinedCalls.contains(call))
			return;

		inlinedCalls.add(call);

		Address address = (Address) call.getPred(1);

		String methodName = address.getEntity().getLdName();
		// address.getGraph().getNr()
		for (Graph graph : Program.getGraphs()) {
			if (methodName.equals(graph.getEntity().getLdName())) {
				Node firstSuccessor = null;

				for (Edge edge : BackEdges.getOuts(call)) {
					if (edge.node.getMode().equals(Mode.getT())) {
						firstSuccessor = edge.node;
						break;
					}
				}

				List<Node> arguments = new ArrayList<>();
				for (int i = 2; i < call.getPredCount(); i++) {
					arguments.add(call.getPred(i));
				}

				BackEdges.disable(call.getGraph());

				GraphInliningCopyOperationVisitor blockCopyWalker = new GraphInliningCopyOperationVisitor(call, arguments);
				graph.walkPostorder(blockCopyWalker);

				BackEdges.enable(call.getGraph());

				addReplacement(call.getPred(2), createBadNode(call.getPred(2)));

				if (firstSuccessor != null) {
					Node oldCallResult = FirmUtils.getFirstSuccessor(firstSuccessor);

					// remove call result
					addReplacement(firstSuccessor, createBadNode(firstSuccessor));
					// write result to new result
					addReplacement(oldCallResult, blockCopyWalker.getResult());
				}

				// replace call with predecessor to keep control flow
				addReplacement(call, call.getPred(0));

				Node useBlock = blockCopyWalker.getLastBlock();
				call.getGraph().walkPostorder(new CorrectBlockVisitor(call, call.getBlock(), useBlock, blockCopyWalker.getCopiedNodes()));

				return;
			}
		}
	}

}
