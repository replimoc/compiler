package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.BlockWalker;
import firm.Graph;
import firm.Mode;
import firm.bindings.binding_irdom;
import firm.nodes.Add;
import firm.nodes.Anchor;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Phi;
import firm.nodes.Proj;

public class OptimizationUtils {
	private final HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private final HashMap<Node, Node> backedges = new HashMap<>();
	private final HashMap<Node, Node> inductionVariables = new HashMap<>();
	private final HashMap<Block, Phi> loopPhis = new HashMap<>();
	private final HashMap<Block, Set<Node>> blockNodes = new HashMap<>();
	private final HashSet<Block> conditionalBlocks = new HashSet<>();
	private boolean calculatedPhis = false;
	private final Graph graph;

	public OptimizationUtils(Graph graph) {
		this.graph = graph;
	}

	public HashSet<Block> getIfBlocks() {
		if (dominators.size() == 0 && backedges.size() == 0 || conditionalBlocks.size() == 0) {
			calculateDominators();
		}
		return conditionalBlocks;
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
		calculatedPhis = true;
		return inductionVariables;
	}

	public HashMap<Block, Phi> getLoopPhis() {
		if (backedges.size() == 0) {
			calculateDominators();
		}
		if (!calculatedPhis)
			calculateInductionVariables();
		return loopPhis;
	}

	public HashMap<Block, Set<Node>> getBlockNodes() {
		if (blockNodes.size() == 0) {
			copyBlocks();
		}
		return blockNodes;
	}

	public Node getInnerMostLoopHeader(Block block) {
		if (dominators.size() == 0 || backedges.size() == 0) {
			calculateDominators();
		}

		Set<Block> dominatorBlocks = dominators.get(block);
		Set<Block> loops = new HashSet<>();
		for (Block dominatorBlock : dominatorBlocks) {
			// find loop header that dominates 'block'
			if (!dominatorBlock.equals(block) && backedges.containsValue(dominatorBlock)) {
				loops.add(dominatorBlock);
			}
		}

		ArrayList<Block> sameLevelLoops = new ArrayList<>();
		L1: for (Block b : loops) {
			if (dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				for (Map.Entry<Node, Node> entry : backedges.entrySet()) {
					if (entry.getValue().equals(b)) {
						if (dominators.containsKey(entry.getKey()) && !dominators.get(entry.getKey()).contains(block)
								&& !dominatorBlocks.contains(entry.getKey())) {
							// b and the looṕ header are on the same 'level'
							sameLevelLoops.add(b);
							continue L1;
						}
					}
				}
			}
		}
		for (Block b : loops) {
			if (!sameLevelLoops.contains(b) && dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				return b;
			}
		}
		return null;
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
				} else if (phi.getMode().equals(Mode.getM())) {
					if (backedges.containsValue(phi.getBlock())) {
						loopPhis.put((Block) phi.getBlock(), phi);
					}
				}

			}
		};
		graph.walk(visitor);
	}

	private void calculateDominators() {
		Node start = graph.getStart();
		binding_irdom.compute_postdoms(graph.ptr);
		binding_irdom.compute_doms(graph.ptr);
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
								if (dominators.get(backedge.node) != null && dominators.get(backedge.node).contains(block)) {
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
					boolean potentialIf = true;
					for (Node node : block.getPreds()) {
						if ((potentialIf && binding_irdom.block_postdominates(block.ptr, node.getBlock().ptr) == 1)
								&& binding_irdom.block_dominates(block.ptr, node.getBlock().ptr) == 0) {
							// no if
							potentialIf = false;
						}
					}
					if (potentialIf) {
						conditionalBlocks.add(block);
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

	private void copyBlocks() {
		AbstractFirmNodesVisitor visitor = new AbstractFirmNodesVisitor() {
			@Override
			protected void visitNode(Node node) {
				Block block = (Block) node.getBlock();
				if (!blockNodes.containsKey(block)) {
					Set<Node> nodes = new HashSet<Node>();
					blockNodes.put(block, nodes);
				}
				Set<Node> nodes = blockNodes.get(block);
				nodes.add(node);
			}
		};
		graph.walk(visitor);
	}

	public Set<LoopInfo> getLoopInfos(Block block, HashMap<Block, Cmp> compares) {
		Set<LoopInfo> loopInfos = new HashSet<>();
		// only unroll the innermost loop
		if (backedges.containsKey(block) && (dominators.get(block).size() == dominators.get(backedges.get(block)).size() + 1)) {
			// loop body
			for (Map.Entry<Node, Node> entry : inductionVariables.entrySet()) {
				if (entry.getKey().getBlock().equals(block.getPred(0).getBlock())) {

					if (getPhiCount((Block) backedges.get(block)) > 2)
						return null;
					// induction variable for this block
					Node node = entry.getValue();
					Node loopCounter = entry.getKey();

					Const incr = getIncrementConstantOrNull(node);
					if (incr == null)
						return null;

					if (!compares.containsKey(block.getPred(0).getBlock()))
						return null;
					Cmp cmp = compares.get(block.getPred(0).getBlock());
					if (cmp == null)
						return null;
					Const constCmp = getConstantCompareNodeOrNull(cmp.getLeft(), cmp.getRight());
					if (constCmp == null)
						return null;

					Const startingValue = getStartingValueOrNull(entry);
					if (startingValue == null)
						return null;

					// get cycle count for loop
					int cycleCount = getCycleCount(cmp, constCmp, startingValue, incr);
					loopInfos.add(new LoopInfo(cycleCount, startingValue, incr, node, loopCounter));
				}
			}
		}
		return loopInfos;
	}

	public class LoopInfo {
		public final int cycleCount;
		public final Const startingValue;
		public final Const incr;
		public final Node node;
		public final Node loopCounter;

		private LoopInfo(int cycleCount, Const startingValue, Const incr, Node node, Node loopCounter) {
			this.cycleCount = cycleCount;
			this.startingValue = startingValue;
			this.incr = incr;
			this.node = node;
			this.loopCounter = loopCounter;
		}
	}

	private Const getIncrementConstantOrNull(Node node) {
		if (node.getPredCount() > 1 && FirmUtils.isConstant(node.getPred(1)) && node instanceof Add) {
			return (Const) node.getPred(1);
		} else if (node.getPredCount() > 1 && FirmUtils.isConstant(node.getPred(0)) && node instanceof Add) {
			return (Const) node.getPred(0);
		} else {
			return null;
		}
	}

	private Const getConstantCompareNodeOrNull(Node left, Node right) {
		if (FirmUtils.isConstant(left) && inductionVariables.containsKey(right)) {
			return (Const) left;
		} else if (FirmUtils.isConstant(right) && inductionVariables.containsKey(left)) {
			return (Const) right;
		} else {
			return null;
		}
	}

	private Const getStartingValueOrNull(Entry<Node, Node> entry) {
		// TODO: discover the starting value through other loops as well
		if (entry.getKey().getPred(0).equals(entry.getValue()) && FirmUtils.isConstant(entry.getKey().getPred(1))) {
			return (Const) entry.getKey().getPred(1);
		} else if (entry.getKey().getPred(1).equals(entry.getValue()) && FirmUtils.isConstant(entry.getKey().getPred(0))) {
			return (Const) entry.getKey().getPred(0);
		} else {
			return null;
		}
	}

	private int getCycleCount(Cmp cmp, Const constCmp, Const startingValue, Const incr) {
		int count = 0;
		switch (cmp.getRelation()) {
		case Less:
			count = (int) Math.ceil((double) (constCmp.getTarval().asInt() - startingValue.getTarval().asInt()) / incr.getTarval().asInt());
			if (incr.getTarval().isNegative()) {
				return count < 0 ? Integer.MIN_VALUE : count;
			} else {
				return count < 0 ? Integer.MAX_VALUE : count;
			}
		case LessEqual:
			count = (int) Math.ceil((double) (constCmp.getTarval().asInt() - startingValue.getTarval().asInt()) / incr.getTarval().asInt());
			if (incr.getTarval().isNegative()) {
				return count <= 0 ? Integer.MIN_VALUE : count + 1;
			} else {
				return count <= 0 ? Integer.MAX_VALUE : count + 1;
			}
		case Greater:
			count = (int) Math.ceil((double) (startingValue.getTarval().asInt() - constCmp.getTarval().asInt()) / incr.getTarval().asInt());
			if (incr.getTarval().isNegative()) {
				return count > 0 ? Integer.MIN_VALUE : count;
			} else {
				return count > 0 ? Integer.MAX_VALUE : count;
			}
		case GreaterEqual:
			count = (int) Math.ceil((double) (startingValue.getTarval().asInt() - constCmp.getTarval().asInt()) / incr.getTarval().asInt());
			if (incr.getTarval().isNegative()) {
				return count >= 0 ? Integer.MIN_VALUE : count - 1;
			} else {
				return count >= 0 ? Integer.MAX_VALUE : count - 1;
			}
		default:
			return 0;
		}
	}

	private int getPhiCount(Block block) {
		// count phi's in inside this block
		int count = 0;
		for (Node node : blockNodes.get(block)) {
			if (node instanceof Phi) {
				count++;
			}
		}
		return count;
	}

}
