package compiler.firm.optimization.visitor.inlining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

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

public class GraphInliningCopyOperationVisitor extends VisitAllNodeVisitor {

	private final Graph graph;
	private HashMap<Node, Node> nodeMapping = new HashMap<>();
	private final Node startNode;
	private Node result;
	private List<Node> arguments;
	private Node lastBlock;
	private HashMap<Node, Node> blockPredecessors = new HashMap<>();
	private LinkedList<Phi> phis = new LinkedList<Phi>();
	private Node startProjM;
	private Node endProjM;

	public GraphInliningCopyOperationVisitor(Node startNode, List<Node> arguments) {
		this.graph = startNode.getGraph();
		this.lastBlock = startNode.getBlock();
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
		return endProjM;
	}

	private Node getMappedNode(Node node) {
		if (!nodeMapping.containsKey(node))
			node.accept(this);

		return nodeMapping.get(node);
	}

	public Node getResult() {
		return result;
	}

	public Node getLastBlock() {
		return lastBlock;
	}

	public Collection<Node> getCopiedNodes() {
		return nodeMapping.values();
	}

	@Override
	protected Node visitNode(Node node) {
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
			startProjM = visitNode(proj);
		} else {
			visitNode(proj);
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

		visitNode(address).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(Const node) {
		if (nodeMapping.containsKey(node))
			return;

		visitNode(node).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(End end) {
		Node endBlock = end.getBlock();

		List<ReturnInfo> returns = new ArrayList<>();
		for (Node pred : endBlock.getPreds()) {
			returns.add(buildReturnInfo(pred));
		}

		ReturnInfo returnInfo = buildReturnInfo(returns, 0, returns.size() - 1);

		result = returnInfo.result;
		endProjM = returnInfo.modeM;
		lastBlock = returnInfo.block;
	}

	private ReturnInfo buildReturnInfo(List<ReturnInfo> returns, int left, int right) {
		int diff = right - left;
		if (diff == 0) {
			return returns.get(left);
		} else {
			ReturnInfo leftReturn = buildReturnInfo(returns, left, diff / 2);
			ReturnInfo rightReturn = buildReturnInfo(returns, diff / 2 + 1, right);

			Node leftJmp = graph.newJmp(leftReturn.block);
			Node rightJmp = graph.newJmp(rightReturn.block);

			Node block = graph.newBlock(new Node[] { leftJmp, rightJmp });

			Node modeM = graph.newPhi(block, new Node[] { leftReturn.modeM, rightReturn.modeM }, Mode.getM());

			Node result = null;
			if (leftReturn.result != null) {
				result = graph.newPhi(block, new Node[] { leftReturn.result, rightReturn.result }, leftReturn.result.getMode());
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
