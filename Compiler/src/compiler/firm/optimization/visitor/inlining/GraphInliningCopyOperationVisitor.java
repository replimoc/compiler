package compiler.firm.optimization.visitor.inlining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import firm.Graph;
import firm.nodes.Address;
import firm.nodes.Block;
import firm.nodes.Const;
import firm.nodes.End;
import firm.nodes.Node;
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

	public GraphInliningCopyOperationVisitor(Node startNode, List<Node> arguments) {
		this.graph = startNode.getGraph();
		this.lastBlock = startNode.getBlock();
		this.startNode = startNode;
		this.arguments = arguments;
	}

	private Node getMappedNode(Node node) {
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
		if (block.equals(block.getGraph().getStartBlock())) {
			nodeMapping.put(block, startNode.getBlock());
		} else {
			List<Node> predecessors = new ArrayList<Node>();
			for (int i = 0; i < block.getPredCount(); i++) {
				if (getMappedNode(block.getPred(i)) != null) {
					predecessors.add(getMappedNode(block.getPred(i)));
				}
			}

			Node[] predecessorsArray = new Node[predecessors.size()];
			predecessors.toArray(predecessorsArray);
			Node newBlock = graph.newBlock(predecessorsArray);
			nodeMapping.put(block, newBlock);
		}
	}

	@Override
	public void visit(Return ret) {
		if (ret.getPredCount() >= 2) {
			lastBlock = nodeMapping.get(ret.getBlock());
			result = nodeMapping.get(ret.getPred(1));
		}
	}

	@Override
	public void visit(Proj proj) {
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
	public void visit(Address arg0) {
		visitNode(arg0).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(Const node) {
		visitNode(node).setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(End arg0) {
	}

}
