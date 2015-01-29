package compiler.firm.optimization.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import firm.BackEdges;
import firm.Entity;
import firm.Graph;
import firm.nodes.Call;
import firm.nodes.Node;

public class ProgramDetails {
	private final HashMap<Entity, EntityDetails> entityDetails = new HashMap<>();

	public boolean hasNoSideEffects(Entity graph) {
		EntityDetails details = entityDetails.get(graph);
		return details != null && details.hasNoSideEffects();
	}

	public EntityDetails getEntityDetails(Entity entity) {
		EntityDetails details = entityDetails.get(entity);
		if (details == null) {
			details = new EntityDetails();
			entityDetails.put(entity, details);
		}
		return details;
	}

	public EntityDetails getEntityDetails(Node node) {
		return getEntityDetails(node.getGraph().getEntity());
	}

	public HashMap<Entity, EntityDetails> getEntityDetails() {
		return entityDetails;
	}

	public List<Call> getCalls() {
		List<Call> calls = new ArrayList<>();
		for (Entry<Entity, EntityDetails> entityDetailEntry : getEntityDetails().entrySet()) {
			EntityDetails entityDetails = entityDetailEntry.getValue();
			for (Entry<Call, CallInformation> callInfo : entityDetails.getCallsToEntity().entrySet()) {
				calls.add(callInfo.getKey());
			}
		}
		return calls;
	}

	public void updateGraph(Graph graph) {
		BackEdges.enable(graph);
		EntityDetails entityDetail = getEntityDetails(graph.getEntity());
		GraphEvaluationVisitor.calculateStaticDetails(graph, entityDetail);
		GraphEvaluationVisitor graphEvaluationVisitor = new GraphEvaluationVisitor(this);
		graph.walk(graphEvaluationVisitor);
		entityDetail.setNumberOfNodes(graphEvaluationVisitor.getNumberOfNodes());
		BackEdges.disable(graph);
	}
}
