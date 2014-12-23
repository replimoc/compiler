package compiler.firm.backend;

import java.util.ArrayList;
import java.util.List;

import firm.nodes.Cond;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Phi;

public class BlockNodes {
	private final List<Node> nodes = new ArrayList<>();
	private final List<Phi> phis = new ArrayList<>();
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

	public void visitNodes(BulkPhiNodeVisitor visitor) {
		for (Node curr : nodes) {
			curr.accept(visitor);
		}

		visitor.visit(phis);

		if (jumpOrCondNode != null) {
			jumpOrCondNode.accept(visitor);
		}
	}

	public void addPhi(Phi phi) {
		phis.add(phi);
	}
}
