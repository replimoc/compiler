package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;

import firm.BackEdges;
import firm.Entity;
import firm.Graph;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Node;

public class ProgramDetails {
	private final HashMap<Entity, EntityDetails> entityDetails = new HashMap<>();

	public boolean hasSideEffects(Entity entity) {
		EntityDetails details = entityDetails.get(entity);
		return details == null || details.hasSideEffects();
	}

	public boolean hasMemUsage(Entity entity) {
		EntityDetails details = entityDetails.get(entity);
		return details == null || details.hasMemUsage();
	}

	public EntityDetails getEntityDetails(Graph graph) {
		return getEntityDetails(graph.getEntity());
	}

	public EntityDetails getEntityDetails(Entity entity) {
		EntityDetails details = entityDetails.get(entity);
		if (details == null) {
			boolean usesMemory = entity.getGraph() == null;
			boolean hasSideEffects = usesMemory && (!entity.getLdName().equals(FirmUtils.CALLOC_PROXY));
			details = new EntityDetails(usesMemory, hasSideEffects);
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
			calls.addAll(entityDetails.getCallsFromEntity().keySet());
		}
		return calls;
	}

	public void updateGraphs(Iterable<Graph> graphs) {
		for (Graph graph : graphs) {
			entityDetails.remove(graph.getEntity());
		}

		for (Graph graph : graphs) {
			updateGraph(graph);
		}
		finishMemoryUsageAndSideEffectCalculation();
		for (EntityDetails entityDetail : entityDetails.values()) {
			updateCallMemoryUsageAndSideEffect(entityDetail);
		}
	}

	private void updateCallMemoryUsageAndSideEffect(EntityDetails entityDetail) {
		for (BlockInformation blockInformation : entityDetail.getBlockInformations().values()) {
			for (Call call : blockInformation.getCalls()) {
				EntityDetails callEntityDetails = getEntityDetails(FirmUtils.getCalledEntity(call));
				if (callEntityDetails.hasMemUsage())
					blockInformation.setHasMemUsage();
				if (callEntityDetails.hasSideEffects())
					blockInformation.setHasSideEffects();
			}
		}
	}

	private void updateGraph(Graph graph) {
		BackEdges.enable(graph);
		EntityDetails entityDetail = getEntityDetails(graph.getEntity());
		GraphEvaluationVisitor.calculateStaticDetails(graph, entityDetail);
		GraphEvaluationVisitor graphEvaluationVisitor = new GraphEvaluationVisitor(graph, this);
		graph.walkTopological(graphEvaluationVisitor);
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
		return entityDetails.get(address.getEntity()).getCallsFromEntity().keySet();
	}

	public void removeCallsToOthers(EntityDetails callingEntity) {
		for (Entry<Call, CallInformation> callFromThis : callingEntity.getCallsFromEntity().entrySet()) {
			Entity calledEntity = callFromThis.getValue().getOtherEntity();
			EntityDetails calledEntityDetails = entityDetails.get(calledEntity);
			calledEntityDetails.removeCall(callFromThis.getKey());
		}
	}

	private void finishMemoryUsageAndSideEffectCalculation() {
		Set<Entity> finishedEntities = new HashSet<>();
		Set<Entity> toDos = entityDetails.keySet();
		Set<Entity> nextToDos = new HashSet<Entity>();

		int runs = 0;
		int maxRuns = entityDetails.size();

		while (!toDos.isEmpty() && runs++ < maxRuns) {
			for (Entity currEntity : toDos) {
				if (finishedEntities.contains(currEntity))
					continue;

				EntityDetails currDetails = entityDetails.get(currEntity);

				boolean finished = false;

				if (currDetails.hasSideEffects()) {
					currDetails.setHasSideEffects();
					currDetails.setHasMemUsage();
					finished = true;

				} else if (currDetails.hasMemUsage()) {
					currDetails.setHasMemUsage(); // don't break here, a side effect could still be found
				}

				int finishedCallees = 0;
				for (Entry<Call, CallInformation> currCallEntry : currDetails.getCallsFromEntity().entrySet()) {
					Entity calledEntity = currCallEntry.getValue().getOtherEntity();
					EntityDetails calledEntityDetails = entityDetails.get(calledEntity);

					if (calledEntityDetails.hasSideEffects()) {
						currDetails.setHasSideEffects();
						currDetails.setHasMemUsage();
						finished = true;
						break;
					} else if (calledEntityDetails.hasMemUsage()) {
						currDetails.setHasMemUsage(); // don't break here, a side effect could still be found
					}
					if (finishedEntities.contains(calledEntity)) {
						finishedCallees++;
					}
				}
				if (finishedCallees == currDetails.getCallsFromEntity().size()) {
					finished = true; // all callees finished but no memory usage or side effects found
				}

				if (finished) {
					finishedEntities.add(currEntity);
				} else {
					nextToDos.add(currEntity);
				}
			}
			toDos = nextToDos;
			nextToDos = new HashSet<Entity>();
		}

		// The elements remaining in the toDo list are part of a recursion => all have memory usage and side effect
		for (Entity currEntity : toDos) {
			EntityDetails entityDetail = entityDetails.get(currEntity);
			entityDetail.setHasMemUsage();
			entityDetail.setHasSideEffects();
		}
	}

}
