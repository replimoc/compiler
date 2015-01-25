package compiler.firm.optimization.visitor.inlining;

import java.util.Collection;

import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Const;
import firm.nodes.Node;

public class CorrectBlockVisitor extends VisitAllNodeVisitor {

	private final Node startOperation;
	private final Node oldBlock;
	private final Node newBlock;
	private boolean doReplacement = false;
	private Collection<Node> newNodes;

	public CorrectBlockVisitor(Node startOperation, Node oldBlock, Node newBlock, Collection<Node> newNodes) {
		this.startOperation = startOperation;
		this.oldBlock = oldBlock;
		this.newBlock = newBlock;
		this.newNodes = newNodes;
	}

	@Override
	protected Node visitNode(Node node) {
		if (doReplacement && oldBlock.equals(node.getBlock()) && !newNodes.contains(node)) {
			node.setBlock(newBlock);
		}
		return node;
	}

	protected void setStartBlock(Node node) {
		if (doReplacement && oldBlock.equals(node.getBlock())) {
			node.setBlock(startOperation.getGraph().getStartBlock());
		}
	}

	@Override
	public void visit(Call call) {
		if (startOperation.equals(call)) {
			doReplacement = true;
		} else {
			visitNode(call);
		}
	}

	@Override
	public void visit(Const constant) {
		setStartBlock(constant);
	}

	@Override
	public void visit(Address address) {
		setStartBlock(address);
	}
}
