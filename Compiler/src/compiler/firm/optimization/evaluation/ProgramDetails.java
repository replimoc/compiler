package compiler.firm.optimization.evaluation;

import java.util.HashMap;

import firm.Entity;

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

	public HashMap<Entity, EntityDetails> getEntityDetails() {
		return entityDetails;
	}

}
