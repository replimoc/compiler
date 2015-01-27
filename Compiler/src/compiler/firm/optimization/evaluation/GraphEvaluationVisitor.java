package compiler.firm.optimization.evaluation;

import java.util.HashSet;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Proj;

public class GraphEvaluationVisitor extends AbstractFirmNodesVisitor {

	public static void calculateStaticDetails(Graph graph, EntityDetails details) {
		boolean hasNoSideEffects = false;
		HashSet<Integer> unusedParameters = new HashSet<Integer>();

		for (Edge startFollower : BackEdges.getOuts(graph.getStart())) {
			Node startFollowerNode = startFollower.node;
			if (startFollowerNode.getMode().equals(Mode.getM())) {
				Proj projM = (Proj) startFollowerNode;

				Iterable<Node> returns = graph.getEndBlock().getPreds();
				for (Node ret : returns) {
					if (!ret.getPred(0).equals(projM)) {
						continue;
					}
				}
				hasNoSideEffects = true;

			} else if (startFollowerNode instanceof Proj && startFollowerNode.getMode().equals(Mode.getT())) {
				MethodType methodType = (MethodType) graph.getEntity().getType();
				int numberOfParams = methodType.getNParams();
				for (int i = 0; i < numberOfParams; i++) {
					unusedParameters.add(i);
				}

				for (Edge parameterEdge : BackEdges.getOuts(startFollowerNode)) {
					Node parameterNode = parameterEdge.node;
					if (parameterNode instanceof Proj) {
						unusedParameters.remove(((Proj) parameterNode).getNum());
					}
				}
			}
		}
		details.setHasNoSideEffects(hasNoSideEffects);
		details.setUnusedParameters(unusedParameters);
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
