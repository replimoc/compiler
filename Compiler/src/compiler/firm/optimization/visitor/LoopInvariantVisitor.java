package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.BlockWalker;
import firm.Graph;
import firm.Mode;
import firm.nodes.Add;
import firm.nodes.Anchor;
import firm.nodes.Block;
import firm.nodes.Conv;
import firm.nodes.Jmp;
import firm.nodes.Minus;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Proj;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Start;
import firm.nodes.Sub;

public class LoopInvariantVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new LoopInvariantVisitor();
		}
	};

	private final HashMap<Node, Node> backedges = new HashMap<>();
	private final HashMap<Block, Set<Block>> dominators = new HashMap<>();

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	private Node getInnerMostLoopHeader(Block block) {
		Set<Block> dominatorBlocks = dominators.get(block);
		Set<Block> loops = new HashSet<>();
		for (Block dominatorBlock : dominatorBlocks) {
			// find loop header that dominates 'block'
			if (!dominatorBlock.equals(block) && backedges.containsValue(dominatorBlock)) {
				loops.add(dominatorBlock);
			}
		}

		for (Block b : loops) {
			if (dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				return b;
			}
		}
		return null;
	}

	@Override
	public void visit(Add add) {
		Add node = (Add) (nodeReplacements.containsKey(add) ? nodeReplacements.get(add) : add);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newAdd(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Sub sub) {
		Sub node = (Sub) (nodeReplacements.containsKey(sub) ? nodeReplacements.get(sub) : sub);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newSub(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Conv conv) {
		Conv node = (Conv) (nodeReplacements.containsKey(conv) ? nodeReplacements.get(conv) : conv);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(operand)) {
					Node copy = node.getGraph().newConv(pred, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shl shl) {
		Shl node = (Shl) (nodeReplacements.containsKey(shl) ? nodeReplacements.get(shl) : shl);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShl(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shr shr) {
		Shr node = (Shr) (nodeReplacements.containsKey(shr) ? nodeReplacements.get(shr) : shr);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShr(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Shrs shrs) {
		Shrs node = (Shrs) (nodeReplacements.containsKey(shrs) ? nodeReplacements.get(shrs) : shrs);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newShrs(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Mul mul) {
		Mul node = (Mul) (nodeReplacements.containsKey(mul) ? nodeReplacements.get(mul) : mul);
		Node left = nodeReplacements.containsKey(node.getLeft()) ? nodeReplacements.get(node.getLeft()).getBlock() : node.getLeft().getBlock();
		Node right = nodeReplacements.containsKey(node.getRight()) ? nodeReplacements.get(node.getRight()).getBlock() : node.getRight().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(left) && doms.contains(right)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(left) && domBorder.contains(right)) {
					Node copy = node.getGraph().newMul(pred, node.getLeft(), node.getRight(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Minus minus) {
		Minus node = (Minus) (nodeReplacements.containsKey(minus) ? nodeReplacements.get(minus) : minus);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(operand)) {
					Node copy = node.getGraph().newMinus(pred, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Not not) {
		Not node = (Not) (nodeReplacements.containsKey(not) ? nodeReplacements.get(not) : not);
		Node operand = nodeReplacements.containsKey(node.getOp()) ? nodeReplacements.get(node.getOp()).getBlock() : node.getOp().getBlock();

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.contains(operand)) {
				Node pred = getInnerMostLoopHeader((Block) node.getBlock());
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(pred);
				if (pred != null && domBorder.contains(operand)) {
					Node copy = node.getGraph().newNot(pred, node.getOp(), node.getMode());
					addReplacement(node, copy);
				}
			}
		}
	}

	@Override
	public void visit(Start start) {
		Graph graph = start.getGraph();
		final Block startBlock = (Block) start.getBlock();

		BlockWalker walker = new BlockWalker() {

			@Override
			public void visitBlock(Block block) {
				Set<Block> doms = new HashSet<Block>();

				for (Node node : block.getPreds()) {
					Block pred = (Block) node.getBlock();
					Set<Block> dominatedBlocks = dominators.get(pred);

					if (dominatedBlocks == null || dominators.get(pred).contains(block))
						continue;

					if (doms.size() == 0) {
						for (Block b : dominatedBlocks) {
							doms.add(b);
						}
					} else {
						doms.retainAll(dominatedBlocks);
					}
				}
				doms.add(block);
				doms.add(startBlock);

				if (!dominators.containsKey(block)) {
					dominators.put(block, doms);
				} else if (!dominators.get(block).equals(doms)) {
					dominators.put(block, doms);
					for (Edge edge : BackEdges.getOuts(block)) {
						if (edge.node instanceof Anchor)
							continue;
						if (((edge.node instanceof Proj && edge.node.getMode().equals(Mode.getX()) || edge.node instanceof Jmp))) {
							for (Edge backedge : BackEdges.getOuts(edge.node)) {
								// visit dominated blocks
								if (dominators.get((Block) backedge.node) != null && dominators.get((Block) backedge.node).contains(block)) {
									visitBlock((Block) backedge.node);
								}
							}
						}
					}
				}

				if (block.getPredCount() > 1) {
					for (Node node : block.getPreds()) {
						Set<Block> tmpBlocks = dominators.get(node.getBlock());
						if (tmpBlocks != null && tmpBlocks.contains(block)) {
							// found back edge to loop header
							backedges.put(node.getBlock(), block);
						}
					}
				}
			}
		};
		// ensure that the fixpoint is reached
		int dominatorCount;
		int backedgesCount;
		do {
			dominatorCount = dominators.values().size();
			backedgesCount = backedges.values().size();
			graph.walkBlocks(walker);
		} while (dominatorCount != dominators.values().size() || backedgesCount != backedges.values().size());
	}
}
