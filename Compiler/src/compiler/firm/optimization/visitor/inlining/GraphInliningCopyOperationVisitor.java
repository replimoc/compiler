package compiler.firm.optimization.visitor.inlining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
	private Set<Phi> phis = new HashSet<Phi>();

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
			Node[] predecessors = new Node[] {
					getMappedNode(phi.getPred(0)),
					getMappedNode(phi.getPred(1))
			};

			Node badNode = getMappedNode(phi);
			Phi newPhi = (Phi) graph.newPhi(getMappedNode(phi.getBlock()), predecessors, phi.getMode());
			phi.setLoop(phi.getLoop());

			Graph.exchange(badNode, newPhi);
		}
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
		if (nodeMapping.containsKey(ret))
			return;

		if (ret.getPredCount() >= 2) {
			lastBlock = nodeMapping.get(ret.getBlock());
			result = nodeMapping.get(ret.getPred(1));
		}
	}

	@Override
	public void visit(Proj proj) {
		if (nodeMapping.containsKey(proj))
			return;

		// Handle arguments
		if (proj.getPred(0).equals(proj.getGraph().getArgs())) {
			nodeMapping.put(proj, this.arguments.get(proj.getNum()));
		} else {
			visitNode(proj);
		}
	}

	@Override
	public void visit(Start start) {
		nodeMapping.put(start, startNode);
	}

	@Override
	public void visit(Address address) {
		visitNode(address).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(Const node) {
		visitNode(node).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(End arg0) {
	}

}
