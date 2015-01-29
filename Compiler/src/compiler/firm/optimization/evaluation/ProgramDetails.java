package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import firm.BackEdges;
import firm.Entity;
import firm.Graph;
import firm.nodes.Address;
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

	public Set<Call> getCalls() {
		Set<Call> calls = new HashSet<>();
		for (Entry<Entity, EntityDetails> entityDetailEntry : getEntityDetails().entrySet()) {
			EntityDetails entityDetails = entityDetailEntry.getValue();
			calls.addAll(entityDetails.getCallsFromEntity());
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

	public boolean isRecursion(Call call) {
		return isRecursion(call, new HashSet<Call>());
	}

	protected boolean isRecursion(Call call, Set<Call> calls) {
		for (Call subcall : getSubcalls(call)) {
			if (!calls.contains(subcall)) {
				calls.add(subcall);
				if (isRecursion(subcall, calls)) {
					return true;
				}
				calls.remove(subcall);
			} else {
				return true;
			}
		}
		return false;
	}

	protected Set<Call> getSubcalls(Call call) {
		if (call.getPredCount() < 2) {
			return new HashSet<>();
		}
		Address address = (Address) call.getPred(1);
		return entityDetails.get(address.getEntity()).getCallsFromEntity();
	}
}
