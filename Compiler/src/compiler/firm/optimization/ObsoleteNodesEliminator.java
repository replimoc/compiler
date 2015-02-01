package compiler.firm.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import compiler.ast.declaration.MainMethodDeclaration;
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

public final class ObsoleteNodesEliminator {
	private ObsoleteNodesEliminator() {
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
				for (Edge parameterEdge : BackEdges.getOuts(startFollowerNode)) {
					if (!(parameterEdge.node instanceof Proj)) {
						continue; // only work on proj nodes
					}
					Proj parameterNode = (Proj) parameterEdge.node;

					if (unusedParameters.contains(parameterNode.getNum())) {
						replacements.put(parameterNode, graph.newBad(parameterNode.getMode()));
					} else {
						Node newProj = graph.newProj(startFollowerNode, parameterNode.getMode(),
								calculateNewParmeterNum(unusedParameters, parameterNode.getNum()));
						replacements.put(parameterNode, newProj);
					}
				}

				break;
			}
		}
		BackEdges.disable(graph);

		FirmUtils.replaceNodes(replacements);
	}

	private static int calculateNewParmeterNum(Set<Integer> unusedParameters, int num) {
		int newIdx = 0;
		for (int i = 0; i < num; i++) {
			if (!unusedParameters.contains(i)) {
				newIdx++;
			}
		}
		return newIdx;
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

	public static boolean eliminateObsoleteNodes(ProgramDetails programDetails) {
		HashMap<Node, Node> replacements = new HashMap<Node, Node>();

		for (Entry<Entity, EntityDetails> curr : programDetails.getEntityDetails().entrySet()) {
			EntityDetails currDetails = curr.getValue();

			if (!currDetails.hasSideEffects()) {
				for (Iterator<Entry<Call, CallInformation>> iterator = currDetails.getCallsToEntity().entrySet().iterator(); iterator.hasNext();) {
					Entry<Call, CallInformation> callEntry = iterator.next();
					Graph callingGraph = callEntry.getValue().getOtherEntity().getGraph();
					BackEdges.enable(callingGraph);
					Call callNode = callEntry.getKey();

					if (BackEdges.getNOuts(callNode) == 1) {
						Node predecessorM = callNode.getMem();
						Node successorM = BackEdges.getOuts(callNode).iterator().next().node;
						replacements.put(successorM, predecessorM);
						replacements.put(callNode, callingGraph.newBad(callNode.getMode()));
						iterator.remove();
					}

					BackEdges.disable(callingGraph);
				}
			}
		}

		FirmUtils.replaceNodes(replacements);

		return replacements.isEmpty();
	}

	public static boolean eliminateObsoleteGraphs(ProgramDetails programDetails) {
		boolean nothingOptimized = true;

		for (Iterator<Entry<Entity, EntityDetails>> iterator = programDetails.getEntityDetails().entrySet().iterator(); iterator.hasNext();) {
			Entry<Entity, EntityDetails> currEntry = iterator.next();
			Entity currEntity = currEntry.getKey();
			EntityDetails currEntityDetails = currEntry.getValue();

			if (currEntityDetails.getCallsToEntity().isEmpty() && !MainMethodDeclaration.MAIN_METHOD_NAME.equals(currEntity.getLdName())) {
				programDetails.removeCallsToOthers(currEntityDetails);
				if (currEntity.getGraph() != null) {
					currEntity.getGraph().free();
				}
				currEntity.free();
				nothingOptimized = false;
				iterator.remove();
			}
		}

		return nothingOptimized;
	}
}
