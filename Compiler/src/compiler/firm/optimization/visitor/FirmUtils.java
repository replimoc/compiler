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
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Phi;
import firm.nodes.Proj;

public class FirmUtils {
	private final HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private final HashMap<Node, Node> backedges = new HashMap<>();
	private final HashMap<Node, Node> inductionVariables = new HashMap<>();
	private final Graph graph;

	public FirmUtils(Graph graph) {
		this.graph = graph;
	}

	public HashMap<Block, Set<Block>> getDominators() {
		if (dominators.size() == 0 && backedges.size() == 0) {
			calculateDominators();
		}
		return dominators;
	}

	public HashMap<Node, Node> getBackEdges() {
		if (dominators.size() == 0 && backedges.size() == 0) {
			calculateDominators();
		}
		return backedges;
	}

	public HashMap<Node, Node> getInductionVariables() {
		if (backedges.size() == 0) {
			calculateDominators();
		}
		calculateInductionVariables();
		return inductionVariables;
	}

	private void calculateInductionVariables() {
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
							inductionVariables.put(phi, node);
						}
					}
				}

			}
		};
		graph.walk(visitor);
	}

	private void calculateDominators() {
		Node start = graph.getStart();
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
