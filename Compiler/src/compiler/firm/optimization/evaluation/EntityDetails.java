package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;

import firm.nodes.Call;

public class EntityDetails {
	private final HashMap<Call, CallInformation> callsToThisEntity = new HashMap<>();
	private final HashMap<Call, CallInformation> callsFromThis = new HashMap<>();
	private boolean hasMemUsage;
	private boolean hasSideEffects;
	private HashSet<Integer> unusedParameters;

	public EntityDetails(boolean hasMemUsage, boolean hasSideEffects) {
		this.hasMemUsage = hasMemUsage;
		this.hasSideEffects = hasSideEffects;
	}

	public boolean hasSideEffects() {
		return hasSideEffects;
	}

	public void setHasSideEffects() {
		this.hasSideEffects = true;
	}

	public void addCallToThisInfo(Call callNode, CallInformation callInformation) {
		callsToThisEntity.put(callNode, callInformation);
	}

	public void addCallFromThisInfo(Call callNode, CallInformation callInformation) {
		callsFromThis.put(callNode, callInformation);
	}

	public HashMap<Call, CallInformation> getCallsToThisEntity() {
		return callsToThisEntity;
	}

	public HashSet<Integer> getUnusedParameters() {
		return unusedParameters;
	}

	public void setUnusedParameters(HashSet<Integer> unusedParameters) {
		this.unusedParameters = unusedParameters;
	}

	public boolean hasMemUsage() {
		return hasMemUsage;
	}

	public void setHasMemUsage() {
		this.hasMemUsage = true;
	}

	public HashMap<Call, CallInformation> getCallsFromThis() {
		return callsFromThis;
	}

}
