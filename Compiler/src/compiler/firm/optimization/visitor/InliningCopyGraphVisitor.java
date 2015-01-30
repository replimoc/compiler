package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.Graph;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Block;
import firm.nodes.Const;
import firm.nodes.End;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Return;
import firm.nodes.Start;

public class InliningCopyGraphVisitor extends AbstractFirmNodesVisitor {

	private final Graph graph;
	private HashMap<Node, Node> nodeMapping = new HashMap<>();
	private final Node startNode;
	private List<Node> arguments;
	private HashMap<Node, Node> blockPredecessors = new HashMap<>();
	private LinkedList<Phi> phis = new LinkedList<Phi>();
	private Node startProjM;
	private ReturnInfo methodReturnInfo;

	public InliningCopyGraphVisitor(Node startNode, List<Node> arguments) {
		this.graph = startNode.getGraph();
		this.startNode = startNode;
		this.arguments = arguments;
	}

	public void cleanupNodes() {
		for (Entry<Node, Node> blockPredecessor : blockPredecessors.entrySet()) {
			if (getMappedNode(blockPredecessor.getKey()) != null) {
				Graph.exchange(blockPredecessor.getValue(), getMappedNode(blockPredecessor.getKey()));
			}
		}

		for (Phi phi : phis) {
			Node[] predecessors = new Node[phi.getPredCount()];
			for (int i = 0; i < phi.getPredCount(); i++) {
				predecessors[i] = getMappedNode(phi.getPred(i));
			}

			Phi newPhi = (Phi) graph.newPhi(getMappedNode(phi.getBlock()), predecessors, phi.getMode());
			newPhi.setLoop(phi.getLoop());

			Graph.exchange(getMappedNode(phi), newPhi);
		}
	}

	public Node getStartProjM() {
		return startProjM;
	}

	public Node getEndProjM() {
		return methodReturnInfo.modeM;
	}

	private Node getMappedNode(Node node) {
		if (!nodeMapping.containsKey(node))
			node.accept(this);

		return nodeMapping.get(node);
	}

	public Node getResult() {
		return methodReturnInfo.result;
	}

	public Node getLastBlock() {
		return methodReturnInfo.block;
	}

	public Collection<Node> getCopiedNodes() {
		return nodeMapping.values();
	}

	@Override
	protected void visitNode(Node node) {
		copyNode(node);
	}

	private Node copyNode(Node node) {
		if (nodeMapping.containsKey(node))
			return nodeMapping.get(node);

		Node copy = graph.copyNode(node);
		copy.setBlock(getMappedNode(node.getBlock()));

		for (int i = 0; i < node.getPredCount(); i++) {
			copy.setPred(i, getMappedNode(node.getPred(i)));
		}

		nodeMapping.put(node, copy);

		return copy;
	}

	@Override
	public void visit(Block block) {
		if (nodeMapping.containsKey(block))
			return;

		if (block.equals(block.getGraph().getStartBlock())) {
			nodeMapping.put(block, startNode.getBlock());
		} else {
			List<Node> predecessors = new ArrayList<Node>();
			for (int i = 0; i < block.getPredCount(); i++) {
				Node badNode = graph.newBad(block.getPred(i).getMode());
				predecessors.add(badNode);
				blockPredecessors.put(block.getPred(i), badNode);
			}

			Node[] predecessorsArray = new Node[predecessors.size()];
			predecessors.toArray(predecessorsArray);
			Node newBlock = graph.newBlock(predecessorsArray);

			nodeMapping.put(block, newBlock);
		}
	}

	@Override
	public void visit(Phi phi) {
		if (nodeMapping.containsKey(phi))
			return;
		// Just create a bad node, we replace it later with a new phi.

		phis.add(phi);
		Mode mode = phi.getMode();
		Node newPhi = graph.newBad(mode);
		nodeMapping.put(phi, newPhi);
	}

	@Override
	public void visit(Return ret) {
	}

	@Override
	public void visit(Proj proj) {
		if (nodeMapping.containsKey(proj))
			return;

		// Handle arguments
		if (proj.getPred(0).equals(proj.getGraph().getArgs())) {
			nodeMapping.put(proj, this.arguments.get(proj.getNum()));
		} else if (proj.getPred(0).equals(proj.getGraph().getStart()) && proj.getMode().equals(Mode.getM())) {
			startProjM = copyNode(proj);
		} else {
			copyNode(proj);
		}
	}

	@Override
	public void visit(Start start) {
		if (nodeMapping.containsKey(start))
			return;

		nodeMapping.put(start, startNode);
	}

	@Override
	public void visit(Address address) {
		if (nodeMapping.containsKey(address))
			return;

		copyNode(address).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(Const node) {
		if (nodeMapping.containsKey(node))
			return;

		copyNode(node).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(End end) {
		Node endBlock = end.getBlock();

		List<ReturnInfo> returns = new ArrayList<>();
		for (int i = 0; i < endBlock.getPredCount(); i++) {
			returns.add(buildReturnInfo(endBlock.getPred(i)));
		}

		methodReturnInfo = buildReturnInfo(returns);
	}

	private ReturnInfo buildReturnInfo(List<ReturnInfo> returns) {
		if (returns.size() == 1) {
			return returns.get(0);
		} else {
			Node[] jumps = new Node[returns.size()];
			Node[] modeMPredecessor = new Node[returns.size()];
			Node[] results = new Node[returns.size()];

			int i = 0;
			for (ReturnInfo ret : returns) {
				jumps[i] = graph.newJmp(ret.block);
				modeMPredecessor[i] = ret.modeM;
				results[i] = ret.result;
				i++;
			}

			Node block = graph.newBlock(jumps);
			Node modeM = graph.newPhi(block, modeMPredecessor, Mode.getM());
			Node result = null;

			if (results[0] != null) {
				result = graph.newPhi(block, results, results[0].getMode());
			}
			return new ReturnInfo(block, modeM, result);
		}
	}

	private static class ReturnInfo {
		public final Node modeM;
		public final Node block;
		public final Node result;

		public ReturnInfo(Node block, Node modeM, Node result) {
			this.modeM = modeM;
			this.block = block;
			this.result = result;
		}
	}

	private ReturnInfo buildReturnInfo(Node ret) {
		Node result = null;
		if (ret.getPredCount() >= 2) {
			result = nodeMapping.get(ret.getPred(1));
		}
		return new ReturnInfo(nodeMapping.get(ret.getBlock()), nodeMapping.get(ret.getPred(0)), result);
	}

}
