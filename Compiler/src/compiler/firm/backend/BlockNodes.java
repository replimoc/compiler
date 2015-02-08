package compiler.firm.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Phi;

public class BlockNodes {
	private final List<Node> nodes = new ArrayList<>();
	private final List<Phi> phis = new ArrayList<>();
	private Jmp jumpNode;
	private Cond condNode;

	public void addNode(Node node) {
		nodes.add(node);
	}

	public void addJump(Jmp jump) {
		assert jumpNode == null && condNode == null;
		jumpNode = jump;
	}

	public void addCond(Cond cond) {
		assert jumpNode == null && condNode == null;
		condNode = cond;
	}

	public void visitNodes(BulkPhiNodeVisitor visitor, HashMap<Block, BlockNodes> blockNodes) {
		for (Node curr : nodes) {
			if (curr instanceof Block) {
				visitor.visit((Block) curr, phis);
			} else if (!(curr instanceof Cmp)) {
				curr.accept(visitor);
			}
		}

		if (jumpNode != null) {
			// handle phis
			BlockNodes nextBlock = blockNodes.get(getNextNode(jumpNode));
			visitor.visit(nextBlock.phis);

			// handle jump
			visitor.visit(jumpNode);

		} else if (condNode != null) {
			// handle all phis of all successors
			List<Phi> phis = new ArrayList<Phi>();
			for (Edge e : BackEdges.getOuts(condNode)) {
				phis.addAll(blockNodes.get(getNextNode(e.node)).phis);
			}
			visitor.visit((Cmp) condNode.getPred(0));

			visitor.visit(phis);

			// handle cond node
			visitor.visit(condNode);
		}
	}

	private Node getNextNode(Node node) {
		return BackEdges.getOuts(node).iterator().next().node;
	}

	public void addPhi(Phi phi) {
		phis.add(phi);
	}
}
