package compiler.firm.optimization.evaluation;

import java.util.HashSet;
import java.util.Set;

import firm.nodes.Call;
import firm.nodes.Node;

public class BlockInformation {
	private Node firstModeM;
	private Node lastModeM;
	private Node endNode;
	private boolean hasMemUsage;
	private boolean hasSideEffects;
	private final Set<Call> calls = new HashSet<>();

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setFirstModeM(Node firstModeM) {
		this.firstModeM = firstModeM;
	}

	public Node getFirstModeM() {
		return firstModeM;
	}

	public void setLastModeM(Node lastModeM) {
		this.lastModeM = lastModeM;
	}

	public Node getLastModeM() {
		return lastModeM;
	}

	public boolean hasMemUsage() {
		return hasMemUsage;
	}

	public void setHasMemUsage() {
		this.hasMemUsage = true;
	}

	public boolean hasSideEffects() {
		return hasSideEffects;
	}

	public void setHasSideEffects() {
		this.hasSideEffects = true;
	}

	public Set<Call> getCalls() {
		return calls;
	}

	public void addCall(Call call) {
		this.calls.add(call);
	}
}
