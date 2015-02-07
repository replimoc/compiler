package compiler.firm.optimization.evaluation;

import java.util.HashSet;
import java.util.Set;

import firm.nodes.Call;
import firm.nodes.Node;
import firm.nodes.Phi;

public class BlockInformation {
	private Node firstModeM;
	private Node lastModeM;
	private Node endNode;
	private int memUsage = 0;
	private int sideEffects = 0;
	private final Set<Call> calls = new HashSet<>();
	private Phi memoryPhi = null;
	private int numberOfNodes = 0;
	private final Set<Node> nodes = new HashSet<>();
	private final Set<Phi> phis = new HashSet<>();

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
		return memUsage > 0;
	}

	public void setHasMemUsage() {
		this.memUsage++;
	}

	public int getMemUsage() {
		return memUsage;
	}

	public boolean hasSideEffects() {
		return sideEffects > 0;
	}

	public void setHasSideEffects() {
		this.sideEffects++;
	}

	public int getSideEffects() {
		return sideEffects;
	}

	public Set<Call> getCalls() {
		return calls;
	}

	public void addCall(Call call) {
		this.calls.add(call);
	}

	public void setMemoryPhi(Phi memoryPhi) {
		this.memoryPhi = memoryPhi;
	}

	public Phi getMemoryPhi() {
		return memoryPhi;
	}

	public void incrementNumberOfNodes() {
		numberOfNodes++;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void addNode(Node node) {
		this.nodes.add(node);
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void addPhi(Phi phi) {
		this.phis.add(phi);
	}

	public Set<Phi> getPhis() {
		return this.phis;
	}
}
