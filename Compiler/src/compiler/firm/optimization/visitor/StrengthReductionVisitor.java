package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.BlockWalker;
import firm.Graph;
import firm.Mode;
import firm.nodes.Anchor;
import firm.nodes.Block;
import firm.nodes.Const;
import firm.nodes.Jmp;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Start;

public class StrengthReductionVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new StrengthReductionVisitor();
		}
	};

	private final HashMap<Node, Node> backedges = new HashMap<>();
	private final HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private final HashMap<Node, Node> inductionVariables = new HashMap<>();

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
	public void visit(Mul mul) {
		Node left = mul.getLeft();
		Node right = mul.getRight();

		if (inductionVariables.containsKey(left)) {
			if (dominators.get((Block) mul.getBlock()).contains(right.getBlock()) && !mul.getBlock().equals(right.getBlock())) {
				// right block dominates this mul -> right comes before the loop header

			}
		} else if (inductionVariables.containsKey(right)) {
			if (dominators.get((Block) mul.getBlock()).contains(left.getBlock()) && !mul.getBlock().equals(left.getBlock())) {
				// left block dominates this mul -> left comes before the loop header
				if (right.getPred(0) != null && isConstant(right.getPred(0))) {
					Node pred = getInnerMostLoopHeader((Block) mul.getBlock());
					if (pred == null)
						return;
					Node preLoopBlock = pred.getPred(0).getBlock();
					if (!dominators.get(left.getBlock()).contains(preLoopBlock)) {
						return;
					}
					Node base = mul.getGraph().newMul(preLoopBlock, right.getPred(0), left, mul.getMode());
					Node incr = inductionVariables.get(right);
					Node constant = null;
					if (incr.getPred(0) != null && !incr.getPred(0).equals(right)) {
						constant = incr.getPred(0);
					} else if (incr.getPred(1) != null && !incr.getPred(1).equals(right)) {
						constant = incr.getPred(1);
					} else {
						return;
					}
					if (!(constant instanceof Const) || !(left instanceof Const)) {
						return;
					}
					Node c = mul.getGraph().newConst(((Const) constant).getTarval().mul(((Const) left).getTarval()));
					Node dummy = mul.getGraph().newDummy(mul.getMode());
					Node loopPhi = mul.getGraph().newPhi(pred, new Node[] { base, dummy }, mul.getMode());
					Node add = mul.getGraph().newAdd(mul.getBlock(), loopPhi, c, mul.getMode());
					loopPhi.setPred(1, add);
					addReplacement(mul, loopPhi);
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

		NodeVisitor visitor = new OptimizationVisitor<Node>() {

			@Override
			public HashMap<Node, Node> getLatticeValues() {
				return nodeReplacements;
			}

			@Override
			public void visit(Phi phi) {
				if (!phi.getMode().equals(Mode.getM()) && backedges.containsValue(phi.getBlock())) {
					Node loopBlock = null;
					for (Entry<Node, Node> entry : backedges.entrySet()) {
						if (entry.getValue().equals(phi.getBlock())) {
							loopBlock = entry.getKey();
						}
					}
					for (Node node : phi.getPreds()) {
						if (node.getBlock().equals(loopBlock)) {
							// found operation inside loop
							/*
							 * for (Edge edge : BackEdges.getOuts(node)) { if (dominators.get(node.getBlock()).contains(edge.node.getBlock())) { //
							 * backedge to loop header return; } }
							 */
							inductionVariables.put(phi, node);
						}
					}
				}

			}
		};
		graph.walk(visitor);
	}
}
