package compiler.firm.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import compiler.firm.FirmUtils;
import firm.BlockWalker;
import firm.Graph;
import firm.nodes.Block;
import firm.nodes.Node;
import firm.nodes.Proj;

public final class FirmGraphTraverser {

	private FirmGraphTraverser() {
	}

	public static void walkLoopOptimizedPostorder(Graph graph, HashMap<Block, BlockInfo> blockInfos, BlockWalker walker) {
		FirmUtils.incrementBlockVisited(graph);
		traverseBlocksGenerationFriendly(graph.getStartBlock(), blockInfos, walker, true);
	}

	public static HashMap<Block, BlockInfo> calculateBlockInfos(Graph graph) {
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

					if (pred instanceof Proj && ((Proj) pred).getNum() == FirmUtils.FALSE) {
						predBlockInfo.followers.addLast(block);
					} else {
						predBlockInfo.followers.addFirst(block);
					}
				}
			}
		});

		FirmUtils.incrementBlockVisited(graph);
		detectLoopHeads(graph.getStartBlock(), blockInfos, new HashSet<Integer>());

		return blockInfos;
	}

	private static void detectLoopHeads(Block block, HashMap<Block, BlockInfo> blockInfos, HashSet<Integer> blockSet) { // FIXME Replace this
		BlockInfo blockInfo = blockInfos.get(block);

		if (blockInfo == null || blockInfo.isLoopHead) {
			return;
		}

		if (block.blockVisited()) {
			if (blockSet.contains(block.getNr())) {
				blockInfo.isLoopHead = true;
				if (blockSet.contains(blockInfo.followers.getLast().getNr())) { // if false case is loop case
					blockInfo.followers.addFirst(blockInfo.followers.removeLast()); // reorder
				}
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

	private static void traverseBlocksGenerationFriendly(Block block, HashMap<Block, BlockInfo> blockInfos, BlockWalker walker, boolean orderLoops) {
		if (block.blockVisited())
			return;
		block.markBlockVisited();

		BlockInfo blockInfo = blockInfos.get(block);
		if (blockInfo == null) {
			walker.visitBlock(block);
			return;
		}

		LinkedList<Block> followers = blockInfo.followers;
		switch (followers.size()) {
		case 1:
			walker.visitBlock(block);
			Block follower = followers.getFirst();
			if (follower.getPredCount() < 2 || allPredecessorsVisited(follower)
					|| (blockInfos.get(follower) != null && blockInfos.get(follower).isLoopHead)) {
				traverseBlocksGenerationFriendly(follower, blockInfos, walker, orderLoops);
			}
			break;

		case 2:
			if (orderLoops && blockInfo.isLoopHead) {
				traverseBlocksGenerationFriendly(followers.getFirst(), blockInfos, walker, orderLoops);
				walker.visitBlock(block);
				traverseBlocksGenerationFriendly(followers.getLast(), blockInfos, walker, orderLoops);
			} else {
				walker.visitBlock(block);
				traverseBlocksGenerationFriendly(followers.getFirst(), blockInfos, walker, orderLoops);
				traverseBlocksGenerationFriendly(followers.getLast(), blockInfos, walker, orderLoops);
			}
			break;

		default:
			throw new RuntimeException("More than 2 followers! " + followers.size());
		}
	}

	private static boolean allPredecessorsVisited(Block block) {
		boolean allPredecessorsVisited = true;

		for (Node curr : block.getPreds()) {
			Block predecessorBlock = (Block) curr.getBlock();
			allPredecessorsVisited &= predecessorBlock.blockVisited();
		}

		return allPredecessorsVisited;
	}

	public static void walkBlocksAllocationFriendly(Graph graph, HashMap<Block, BlockInfo> blockInfos, BlockWalker walker) {
		FirmUtils.incrementBlockVisited(graph);
		traverseBlocksGenerationFriendly(graph.getStartBlock(), blockInfos, walker, false);
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
