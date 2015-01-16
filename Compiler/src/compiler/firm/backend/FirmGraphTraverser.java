package compiler.firm.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import firm.BlockWalker;
import firm.Graph;
import firm.bindings.binding_irgraph;
import firm.nodes.Block;
import firm.nodes.Node;

public final class FirmGraphTraverser {

	private FirmGraphTraverser() {
	}

	public static HashMap<Block, BlockInfo> walkBlocksPostOrder(Graph graph, BlockWalker walker) {
		HashMap<Block, BlockInfo> blockFollowers = calculateBlockFollowers(graph);

		incrementBlockVisited(graph);
		traverseBlocksDepthFirst(graph.getStartBlock(), blockFollowers, walker);

		return blockFollowers;
	}

	public static void walkLoopOptimizedPostorder(Graph graph, HashMap<Block, BlockInfo> blockFollowers, BlockWalker walker) {
		incrementBlockVisited(graph);
		detectLoopHeads(graph.getStartBlock(), blockFollowers, new HashSet<Integer>());

		incrementBlockVisited(graph);
		traverseBlocksDepthFirst(graph.getStartBlock(), blockFollowers, walker);
	}

	private static void incrementBlockVisited(Graph graph) {
		binding_irgraph.inc_irg_block_visited(graph.ptr);
	}

	private static HashMap<Block, BlockInfo> calculateBlockFollowers(Graph graph) {
		final HashMap<Block, BlockInfo> blockInfos = new HashMap<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				for (Node pred : block.getPreds()) {
					Block predBlock = (Block) pred.getBlock();

					BlockInfo predBlockInfo = blockInfos.get(predBlock);
					if (predBlockInfo == null) {
						predBlockInfo = new BlockInfo();
						blockInfos.put(predBlock, predBlockInfo);
					}

					// adding the block at the beginning ensures that the loop body comes after the head
					// adding the block to the end of the list makes depth first to the end block and then the loop body
					predBlockInfo.followers.addFirst(block);
				}
			}
		});
		return blockInfos;
	}

	private static void detectLoopHeads(Block block, HashMap<Block, BlockInfo> blockInfos, HashSet<Integer> blockSet) {
		BlockInfo blockInfo = blockInfos.get(block);

		if (blockInfo == null || blockInfo.isLoopHead) {
			return;
		}

		if (block.blockVisited()) {
			if (blockSet.contains(block.getNr())) {
				blockInfo.isLoopHead = true;
			}
			return;
		}
		block.markBlockVisited();

		blockSet.add(block.getNr());
		for (Block followerBlock : blockInfo.followers) {
			detectLoopHeads(followerBlock, blockInfos, blockSet);
		}
		blockSet.remove(block.getNr());
	}

	private static void traverseBlocksDepthFirst(Block block, HashMap<Block, BlockInfo> blockInfos, BlockWalker walker) {
		if (block.blockVisited())
			return;
		block.markBlockVisited();

		BlockInfo blockInfo = blockInfos.get(block);
		if (blockInfo == null) {
			walker.visitBlock(block);
			return;
		}

		if (blockInfo.isLoopHead) {
			Iterator<Block> iter = blockInfo.followers.iterator();
			traverseBlocksDepthFirst(iter.next(), blockInfos, walker);
			walker.visitBlock(block);
			while (iter.hasNext()) {
				traverseBlocksDepthFirst(iter.next(), blockInfos, walker);
			}
		} else {
			walker.visitBlock(block);
			for (Block followerBlock : blockInfo.followers) {
				traverseBlocksDepthFirst(followerBlock, blockInfos, walker);
			}
		}
	}

	public static class BlockInfo {
		LinkedList<Block> followers = new LinkedList<>();
		boolean isLoopHead;

		@Override
		public String toString() {
			return followers.toString();
		}
	}
}
