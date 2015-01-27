package compiler.firm.optimization.evaluation;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Proj;

public class GraphEvaluationVisitor extends AbstractFirmNodesVisitor {

	public static boolean hasNoSideEffects(Graph graph) {
		for (Edge startFollower : BackEdges.getOuts(graph.getStart())) {
			if (startFollower.node.getMode().equals(Mode.getM())) {
				Proj projM = (Proj) startFollower.node;

				Iterable<Node> returns = graph.getEndBlock().getPreds();
				for (Node ret : returns) {
					if (!ret.getPred(0).equals(projM)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private final ProgramDetails programDetails;

	public GraphEvaluationVisitor(ProgramDetails programDetails) {
		this.programDetails = programDetails;
	}

	@Override
	public void visit(Call callNode) {
		int constantArguments = 0;
		for (int i = 2; i < callNode.getPredCount(); i++) {
			if (callNode.getPred(i) instanceof Const) {
				constantArguments++;
			}
		}

		final Address address = (Address) callNode.getPred(1);
		Entity entity = address.getEntity();
		EntityDetails entityDetails = programDetails.getEntityDetails(entity);
		entityDetails.addCallInfo(callNode, constantArguments);
	}
}
