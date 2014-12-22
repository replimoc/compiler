package compiler.firm.backend;

import java.util.ArrayList;
import java.util.List;

import firm.nodes.Cond;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;

public class BlockNodes {
	private final List<Node> nodes = new ArrayList<Node>();
	private Node jumpOrCondNode;

	public void addNode(Node node) {
		nodes.add(node);
	}

	public void addJump(Jmp jump) {
		assert jumpOrCondNode == null;
		jumpOrCondNode = jump;
	}

	public void addCond(Cond cond) {
		assert jumpOrCondNode == null;
		jumpOrCondNode = cond;
	}

	public void visitNodes(NodeVisitor visitor) {
		for (Node curr : nodes) {
			curr.accept(visitor);
		}
		if (jumpOrCondNode != null) {
			jumpOrCondNode.accept(visitor);
		}
	}
}
