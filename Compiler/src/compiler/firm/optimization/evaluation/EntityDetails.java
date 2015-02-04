package compiler.firm.optimization.evaluation;

import java.util.HashMap;
import java.util.HashSet;

import firm.nodes.Call;
import firm.nodes.Node;

public class EntityDetails {
	private final HashMap<Call, CallInformation> callsToEntity = new HashMap<>();
	private final HashMap<Call, CallInformation> callsFromEntity = new HashMap<>();
	private final HashMap<Node, BlockInformation> blockInformation = new HashMap<>();
	private boolean hasMemUsage;
	private boolean hasSideEffects;
	private HashSet<Integer> unusedParameters;
	private int numberOfNodes = 0;

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

	public void addCallToEntityInfo(Call callNode, CallInformation callInformation) {
		callsToEntity.put(callNode, callInformation);
	}

	public void addCallFromEntityInfo(Call callNode, CallInformation callInformation) {
		callsFromEntity.put(callNode, callInformation);
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

	public boolean hasMemUsage() {
		return hasMemUsage;
	}

	public void setHasMemUsage() {
		this.hasMemUsage = true;
	}

	public HashMap<Call, CallInformation> getCallsFromEntity() {
		return callsFromEntity;
	}

	public void removeCall(Call call) {
		callsToEntity.remove(call);
	}

	public void addBlockInfo(Node block, BlockInformation information) {
		blockInformation.put(block, information);
	}

	public BlockInformation getBlockInformation(Node block) {
		BlockInformation blockInfo = blockInformation.get(block);
		if (blockInfo == null) {
			blockInfo = new BlockInformation();
			blockInformation.put(block, blockInfo);
		}
		return blockInfo;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public boolean isInlinable() {
		return numberOfNodes < 100 || callsToEntity.size() <= 1;
	}
}
