package compiler.firm.optimization.visitor.inlining;

import java.util.HashSet;
import java.util.Set;

import firm.nodes.Address;
import firm.nodes.Const;
import firm.nodes.Node;

public class GetNodesForBlockVisitor extends VisitAllNodeVisitor {

	private final Node block;
	private final Set<Node> nodes = new HashSet<>();

	public GetNodesForBlockVisitor(Node block) {
		this.block = block;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	@Override
	protected Node visitNode(Node node) {
		if (node.getBlock() != null && node.getBlock().equals(block)) {
			nodes.add(node);
		}
		return node;
	}

	@Override
	public void visit(Const constant) {
	}

	@Override
	public void visit(Address address) {
	}

}
