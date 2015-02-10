package compiler.firm.optimization.visitor;

import java.util.Set;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.nodes.Block;
import firm.nodes.Node;

public class BlockFilterVisitor extends AbstractFirmNodesVisitor {
	private final Set<Block> blocks;
	private final AbstractFirmNodesVisitor visitor;

	public BlockFilterVisitor(AbstractFirmNodesVisitor visitor, Set<Block> blocks) {
		this.blocks = blocks;
		this.visitor = visitor;
	}

	@Override
	protected void visitNode(Node node) {
		if (node instanceof Block && blocks.contains(node)) {
			node.accept(visitor);
		} else if (blocks.contains(node.getBlock())) {
			node.accept(visitor);
		}
	}
}
