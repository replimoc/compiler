package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;

import firm.Entity;
import firm.Graph;
import firm.nodes.Call;

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

	public HashMap<Entity, EntityDetails> getEntityDetails() {
		return entityDetails;
	}

	public void finishMemoryUsageAndSideEffectCalculation() {
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
					currDetails.setHasMemUsage();
					finished = true;

				} else if (currDetails.getCallsFromThis().isEmpty()) {
					finished = true;

				} else {
					int finishedCallees = 0;
					for (Entry<Call, CallInformation> currCallEntry : currDetails.getCallsFromThis().entrySet()) {
						Entity calledEntity = currCallEntry.getValue().getOtherEntity();
						EntityDetails calledEntityDetails = entityDetails.get(calledEntity);

						if (calledEntityDetails.hasSideEffects()) {
							currDetails.setHasSideEffects();
							currDetails.setHasMemUsage();
							finished = true;
							break;
						} else if (calledEntityDetails.hasMemUsage()) {
							currDetails.setHasMemUsage();
							finished = true;
							break;
						} else if (finishedEntities.contains(calledEntity)) {
							finishedCallees++;
						}
					}
					if (finishedCallees == currDetails.getCallsFromThis().size()) {
						finished = true; // all callees finished but no memory usage or side effects found
					}
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
