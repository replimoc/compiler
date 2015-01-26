package compiler.firm.optimization.visitor.inlining;

import java.util.Collection;

import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Cond;
import firm.nodes.Const;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Proj;

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
		if ((doReplacement || hasModeM(node)) && oldBlock.equals(node.getBlock()) && !newNodes.contains(node)) {
			node.setBlock(newBlock);
		}
		return node;
	}

	private boolean hasModeM(Node node) {
		if (node.getPredCount() > 0 && node.getPred(0).getMode().equals(Mode.getM())) {
			Node predecessor = node;
			while (predecessor != null && predecessor.getPredCount() > 0) {
				if (predecessor.equals(startOperation)) {
					return true;
				}
				predecessor = predecessor.getPred(0);
			}
		}
		return false;
	}

	protected void correctBlock(Node node) {
		if (oldBlock.equals(node.getBlock()) && !newNodes.contains(node)) {
			node.setBlock(newBlock);
		}
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

	@Override
	public void visit(Cond node) {
		correctBlock(node);
	}

	@Override
	public void visit(Proj node) {
		if (node.getMode().equals(Mode.getX())) {
			correctBlock(node);
		} else {
			visitNode(node);
		}
	}

	@Override
	public void visit(Jmp node) {
		correctBlock(node);
	}
}
