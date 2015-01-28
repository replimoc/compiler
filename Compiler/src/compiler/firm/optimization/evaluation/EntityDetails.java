package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;

import firm.nodes.Call;

public class EntityDetails {
	private final HashMap<Call, CallInformation> callsToEntity = new HashMap<>();
	private boolean hasNoSideEffects;
	private HashSet<Integer> unusedParameters;

	public boolean hasNoSideEffects() {
		return hasNoSideEffects;
	}

	public void setHasNoSideEffects(boolean hasNoSideEffects) {
		this.hasNoSideEffects = hasNoSideEffects;
	}

	public void addCallInfo(Call callNode, int constantArguments) {
		callsToEntity.put(callNode, new CallInformation(constantArguments));
	}

	public HashMap<Call, CallInformation> getCallsToEntity() {
		return callsToEntity;
	}

	public HashSet<Integer> getUnusedParameters() {
		return unusedParameters;
	}

	public void setUnusedParameters(HashSet<Integer> unusedParameters) {
		this.unusedParameters = unusedParameters;
	}

}
