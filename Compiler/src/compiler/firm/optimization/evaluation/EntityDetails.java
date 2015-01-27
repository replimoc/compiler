package compiler.firm.optimization.evaluation;

import java.util.HashMap;

import firm.nodes.Call;

public class EntityDetails {
	private final HashMap<Call, CallInformation> callsToEntity = new HashMap<>();
	private boolean hasNoSideEffects;

	public boolean hasNoSideEffects() {
		return hasNoSideEffects;
	}

	public void setHasNoSideEffects(boolean hasNoSideEffects) {
		this.hasNoSideEffects = hasNoSideEffects;
	}

	public void addCallInfo(Call callNode, int constantArguments) {
		callsToEntity.put(callNode, new CallInformation(constantArguments));
	}
}
