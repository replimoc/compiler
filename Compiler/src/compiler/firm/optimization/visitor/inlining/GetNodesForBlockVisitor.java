package compiler.firm.optimization.visitor.inlining;

import firm.nodes.Address;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Proj;
import firm.nodes.Return;

public class GetNodesForBlockVisitor extends VisitAllNodeVisitor {

	private final Node block;
	private Node end;

	public GetNodesForBlockVisitor(Node block) {
		this.block = block;
	}

	public Node getEnd() {
		return end;
	}

	@Override
	protected Node visitNode(Node node) {
		return node;
	}

	@Override
	public void visit(Const constant) {
	}

	@Override
	public void visit(Address address) {
	}

	@Override
	public void visit(Proj proj) {
		if (!proj.getPred(0).equals(proj.getGraph().getArgs())) {
			visitNode(proj);
		}
	}

	private void collectEnd(Node node) {
		if (node.getBlock().equals(block)) {
			this.end = node;
		}
	}

	@Override
	public void visit(Cmp cmp) {
		collectEnd(cmp);
	}

	@Override
	public void visit(Jmp jmp) {
		collectEnd(jmp);
	}

	@Override
	public void visit(Return ret) {
		collectEnd(ret);
	}
}
