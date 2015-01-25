package compiler.firm.optimization.visitor.inlining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import firm.Graph;
import firm.nodes.Add;
import firm.nodes.Address;
import firm.nodes.Align;
import firm.nodes.Alloc;
import firm.nodes.Anchor;
import firm.nodes.And;
import firm.nodes.Bad;
import firm.nodes.Bitcast;
import firm.nodes.Block;
import firm.nodes.Builtin;
import firm.nodes.Call;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Confirm;
import firm.nodes.Const;
import firm.nodes.Conv;
import firm.nodes.CopyB;
import firm.nodes.Deleted;
import firm.nodes.Div;
import firm.nodes.Dummy;
import firm.nodes.End;
import firm.nodes.Eor;
import firm.nodes.Free;
import firm.nodes.IJmp;
import firm.nodes.Id;
import firm.nodes.Jmp;
import firm.nodes.Load;
import firm.nodes.Member;
import firm.nodes.Minus;
import firm.nodes.Mod;
import firm.nodes.Mul;
import firm.nodes.Mulh;
import firm.nodes.Mux;
import firm.nodes.NoMem;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Not;
import firm.nodes.Offset;
import firm.nodes.Or;
import firm.nodes.Phi;
import firm.nodes.Pin;
import firm.nodes.Proj;
import firm.nodes.Raise;
import firm.nodes.Return;
import firm.nodes.Sel;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Size;
import firm.nodes.Start;
import firm.nodes.Store;
import firm.nodes.Sub;
import firm.nodes.Switch;
import firm.nodes.Sync;
import firm.nodes.Tuple;
import firm.nodes.Unknown;

public class GraphInliningCopyOperationVisitor implements NodeVisitor {

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

	private Node copyNode(Node node) {
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

			graph.keepAlive(newBlock);
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
			System.out.println("Argument " + proj + " is " + nodeMapping.get(proj));
		} else {
			copyNode(proj);
		}
	}

	@Override
	public void visit(Start start) {
		nodeMapping.put(start, startNode);
	}

	@Override
	public void visit(End arg0) {
	}

	@Override
	public void visit(Add node) {
		copyNode(node);
	}

	@Override
	public void visit(Address arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Align arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Alloc arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Anchor arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(And arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Bad arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Bitcast arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Builtin arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Call arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Cmp arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Cond arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Confirm arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Const node) {
		Node constant = copyNode(node);
		constant.setBlock(graph.getStartBlock());
	}

	@Override
	public void visit(Conv arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(CopyB arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Deleted arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Div arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Dummy arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Eor arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Free arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(IJmp arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Id arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Jmp arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Load arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Member arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Minus arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Mod arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Mul arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Mulh arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Mux arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(NoMem arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Not arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Offset arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Or arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Phi arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Pin arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Raise arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Sel arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Shl arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Shr arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Shrs arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Size arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Store arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Sub arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Switch arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Sync arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Tuple arg0) {
		copyNode(arg0);
	}

	@Override
	public void visit(Unknown arg0) {
		copyNode(arg0);
	}

	@Override
	public void visitUnknown(Node arg0) {
		copyNode(arg0);
	}

}
