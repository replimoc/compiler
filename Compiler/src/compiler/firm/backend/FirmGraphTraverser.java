package compiler.firm.backend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import firm.BlockWalker;
import firm.Graph;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Node;

public final class FirmGraphTraverser {

	private FirmGraphTraverser() {
	}

	public static void walkBlocksPostOrder(Graph graph, BlockWalker walker) {
		HashMap<Block, ? extends List<Block>> blockFollowers = calculateBlockFollowers(graph);
		incrementBlockVisited(graph);
		traverseBlocksDepthFirst(graph.getStartBlock(), blockFollowers, walker);
	}

	private static void incrementBlockVisited(Graph graph) {
		binding_irgraph.inc_irg_block_visited(graph.ptr);
	}

	private static HashMap<Block, ? extends List<Block>> calculateBlockFollowers(Graph graph) {
		final HashMap<Block, LinkedList<Block>> blockFollowers = new HashMap<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				for (Node pred : block.getPreds()) {
					Block predBlock = (Block) pred.getBlock();

					LinkedList<Block> predsNextBlocks = blockFollowers.get(predBlock);
					if (predsNextBlocks == null) {
						predsNextBlocks = new LinkedList<Block>();
						blockFollowers.put(predBlock, predsNextBlocks);
					}

					// adding the block at the beginning ensures that the loop body comes after the head
					// adding the block to the end of the list makes depth first to the end block and then the loop body
					predsNextBlocks.addFirst(block);
				}
			}
		});
		return blockFollowers;
	}

	private static void traverseBlocksDepthFirst(Block block, HashMap<Block, ? extends List<Block>> nextBlocks, BlockWalker walker) {
		block.markBlockVisited();
		walker.visitBlock(block);

		List<Block> followers = nextBlocks.get(block);

		if (followers == null)
			return;

		for (Block followerBlock : followers) {
			if (!followerBlock.blockVisited())
				traverseBlocksDepthFirst(followerBlock, nextBlocks, walker);
		}
	}
}
