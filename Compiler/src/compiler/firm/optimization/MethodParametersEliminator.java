package compiler.firm.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.evaluation.CallInformation;
import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Program;
import firm.Type;
import firm.nodes.Call;
import firm.nodes.Node;
import firm.nodes.Proj;

public final class MethodParametersEliminator {
	private MethodParametersEliminator() {
	}

	public static boolean eliminateObsoleteParameters(ProgramDetails programDetails) {
		boolean nothingOptimized = true;

		for (Entry<Entity, EntityDetails> entityDetailEntry : programDetails.getEntityDetails().entrySet()) {
			Entity entity = entityDetailEntry.getKey();
			EntityDetails graphDetails = entityDetailEntry.getValue();
			HashSet<Integer> unusedParameters = graphDetails.getUnusedParameters();

			if (unusedParameters != null && !unusedParameters.isEmpty()) {
				eliminateParametersOfEntity(entity, unusedParameters, graphDetails.getCallsToEntity());
				nothingOptimized = false;
			}
		}

		for (Graph graph : Program.getGraphs()) {
			FirmUtils.removeBadsAndUnreachable(graph);
		}

		return nothingOptimized;
	}

	private static void eliminateParametersOfEntity(Entity entity, Set<Integer> unusedParameters, HashMap<Call, CallInformation> callsToEntity) {
		Graph graph = entity.getGraph();

		MethodType newMethodType = createNewMethodType(entity, unusedParameters);
		entity.setType(newMethodType);

		exchangeParameterProjs(graph, unusedParameters);
		removeCallParameters(newMethodType, callsToEntity, unusedParameters);
	}

	private static void removeCallParameters(MethodType newMethodType, HashMap<Call, CallInformation> callsToEntity, Set<Integer> unusedParameters) {

		for (Entry<Call, CallInformation> callEntry : callsToEntity.entrySet()) {
			Call call = callEntry.getKey();

			Node[] parameters = new Node[call.getPredCount() - 2 - unusedParameters.size()];

			for (int i = 2, newIdx = 0; i < call.getPredCount(); i++) {
				int paramIdx = i - 2;
				if (!unusedParameters.contains(paramIdx)) {
					parameters[newIdx++] = call.getPred(i);
				}
			}

			Node newCall = call.getGraph().newCall(call.getBlock(), call.getMem(), call.getPtr(), parameters, newMethodType);
			Graph.exchange(call, newCall);
		}

	}

	private static void exchangeParameterProjs(Graph graph, Set<Integer> unusedParameters) {
		HashMap<Node, Node> replacements = new HashMap<Node, Node>();
		BackEdges.enable(graph);
		for (Edge startFollower : BackEdges.getOuts(graph.getStart())) {
			Node startFollowerNode = startFollower.node;
			if (startFollowerNode instanceof Proj && startFollowerNode.getMode().equals(Mode.getT())) {

				HashMap<Integer, Integer> nodeMapping = new HashMap<>(); // build a mapping ahead, because BackEdges have no order
				for (int i = 0, newIdx = 0; i < BackEdges.getNOuts(startFollowerNode); i++) { // Note: mapping is bigger than needed
					if (!unusedParameters.contains(i)) {
						nodeMapping.put(i, newIdx++);
					}
				}

				for (Edge parameterEdge : BackEdges.getOuts(startFollowerNode)) {
					if (!(parameterEdge.node instanceof Proj)) {
						continue; // only work on proj nodes
					}
					Proj parameterNode = (Proj) parameterEdge.node;

					if (unusedParameters.contains(parameterNode.getNum())) {
						replacements.put(parameterNode, graph.newBad(parameterNode.getMode()));
					} else {
						Node newProj = graph.newProj(startFollowerNode, parameterNode.getMode(), nodeMapping.get(parameterNode.getNum()));
						replacements.put(parameterNode, newProj);
					}
				}

				break;
			}
		}
		BackEdges.disable(graph);

		FirmUtils.replaceNodes(replacements);
	}

	private static MethodType createNewMethodType(Entity entity, Set<Integer> unusedParameters) {
		MethodType methodType = (MethodType) entity.getType();

		Type[] newParameterTypes = new Type[methodType.getNParams() - unusedParameters.size()];

		for (int oldIdx = 0, newIdx = 0; oldIdx < methodType.getNParams(); oldIdx++) {
			if (!unusedParameters.contains(oldIdx)) {
				newParameterTypes[newIdx++] = methodType.getParamType(oldIdx);
			}
		}

		firm.Type[] returnType = {};
		if (methodType.getNRess() > 0) {
			returnType = new firm.Type[] { methodType.getResType(0) };
		}

		return new MethodType(newParameterTypes, returnType);
	}
}
