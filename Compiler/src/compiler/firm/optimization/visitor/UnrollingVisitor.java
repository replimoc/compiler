package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import firm.BackEdges;
import firm.Graph;
import firm.Mode;
import firm.TargetValue;
import firm.nodes.Add;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Start;

public class UnrollingVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new UnrollingVisitor();
		}
	};

	// private static final Set<Block> finishedLoops = new HashSet<>();
	private static boolean finished = false;

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private HashMap<Node, Node> inductionVariables = new HashMap<>();
	private final HashMap<Block, Cmp> compares = new HashMap<>();
	private HashMap<Block, Set<Node>> blockNodes = new HashMap<>();
	private HashMap<Block, Phi> loopPhis = new HashMap<>();
	private static final int MAX_UNROLL_FACTOR = 8;

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	@Override
	public void visit(Cmp cmp) {
		if (!compares.containsKey((Block) cmp.getBlock())) {
			compares.put((Block) cmp.getBlock(), cmp);
			cmp.getBlock().accept(this);
		}
	}

	@Override
	public void visit(Block block) {
		if (finished)
			return;

		// only unroll the innermost loop
		if (backedges.containsKey(block) && (dominators.get(block).size() == dominators.get(backedges.get(block)).size() + 1)) {
			// loop body
			for (Map.Entry<Node, Node> entry : inductionVariables.entrySet()) {
				if (entry.getKey().getBlock().equals(block.getPred(0).getBlock())) {

					if (getPhiCount((Block) backedges.get(block)) > 2)
						return;
					// induction variable for this block
					Node node = entry.getValue();
					Const incr = getIncrementConstantOrNull(node);
					if (incr == null)
						return;

					if (!compares.containsKey(block.getPred(0).getBlock()))
						return;
					Cmp cmp = compares.get(block.getPred(0).getBlock());
					if (cmp == null)
						return;
					Const constCmp = getConstantCompareNodeOrNull(cmp.getLeft(), cmp.getRight());
					if (constCmp == null)
						return;

					Const startingValue = getStartingValueOrNull(entry);
					if (startingValue == null)
						return;

					// get cycle count for loop
					int cycleCount = getCycleCount(cmp, constCmp, startingValue, incr);
					// negative cycle count means the counter is descending
					if (Math.abs(cycleCount) < 2)
						return;

					int unrollFactor = MAX_UNROLL_FACTOR;
					while (unrollFactor > 1 && (Math.abs(cycleCount) % unrollFactor) != 0) {
						unrollFactor -= 1;
					}

					Graph graph = block.getGraph();
					// unroll if block generates overflow
					if (cycleCount == Integer.MAX_VALUE) {
						long target = Integer.MAX_VALUE - startingValue.getTarval().asLong() + incr.getTarval().asLong();
						unrollFactor = MAX_UNROLL_FACTOR;
						while (unrollFactor > 1 && (target % unrollFactor) != 0) {
							unrollFactor -= 1;
						}
						if (unrollFactor < 2)
							return;
					} else if (cycleCount == Integer.MIN_VALUE) {
						long target = Integer.MIN_VALUE + startingValue.getTarval().asLong() - incr.getTarval().asLong();
						unrollFactor = MAX_UNROLL_FACTOR;
						while (unrollFactor > 1 && (target % unrollFactor) != 0) {
							unrollFactor -= 1;
						}
						if (unrollFactor < 2)
							return;
					} else if (unrollFactor < 2 || (Math.abs(cycleCount) % unrollFactor) != 0) {
						return;
					}

					// counter
					Node loopCounter = entry.getKey();
					HashMap<Node, Node> changedNodes = new HashMap<>();
					Node loopPhi = loopPhis.get(backedges.get(block));

					if (loopPhi.getPredCount() > 2)
						return;

					// don't unroll too big blocks
					if (blockNodes.get(block).size() > 30)
						return;

					// replace the increment operation
					addReplacement(incr, graph.newConst(incr.getTarval().mul(new TargetValue(unrollFactor, incr.getMode()))));

					unroll(block, incr, loopCounter, node, changedNodes, loopCounter, loopPhi, unrollFactor);

					// finishedLoops.add(block);
					finished = true;
				}
			}
		}
	}

	private void unroll(Block block, Const incr, Node loopCounter, Node node, HashMap<Node, Node> changedNodes, Node counter, Node loopPhi,
			int unrollFactor) {
		Graph graph = block.getGraph();
		Node firstMemNode = loopPhi;
		Node lastMemNode = loopPhi.getPred(1);
		HashMap<Node, Node> inductions = new HashMap<Node, Node>();
		for (Entry<Node, Node> entry : inductionVariables.entrySet()) {
			inductions.put(entry.getKey(), entry.getValue());
		}

		for (int i = 1; i < unrollFactor; i++) {
			// create the 'i + 1' increment node for the new iteration
			Node count = graph.newAdd(block, loopCounter,
					graph.newConst(incr.getTarval().mul(new TargetValue(i, incr.getMode()))), loopCounter.getMode());
			count.setBlock(block);

			copyBlockNodes(changedNodes, block, node);

			// adjust all predecessors
			for (Entry<Node, Node> nodeEntry : changedNodes.entrySet()) {
				if (!nodeEntry.getKey().equals(node)) {
					Node blockNode = nodeEntry.getKey();
					Node copy = nodeEntry.getValue();

					// check dependencies for unrolled nodes
					for (int j = 0; j < blockNode.getPredCount(); j++) {
						if (blockNode.getPred(j).equals(counter)) {
							// node is before the loop increment
							copy.setPred(j, count);
						} else if (blockNode.getPred(j).equals(node)) {
							// node is after the loop increment
							blockNode.setPred(j, count);
						} else if (changedNodes.containsKey(blockNode.getPred(j))) {
							copy.setPred(j, changedNodes.get(blockNode.getPred(j)));
						} else if (blockNode.getPred(j).equals(firstMemNode)) {
							// adjust memory flow
							if (blockNode.getPred(j).getMode().equals(Mode.getM())) {
								copy.setPred(j, lastMemNode);
								if (!firstMemNode.equals(loopPhi)) {
									firstMemNode = changedNodes.get(firstMemNode);
								} else {
									firstMemNode = copy;
								}
							}
						} else if (blockNode.getPred(j).equals(lastMemNode)) {
							// adjust memory flow
							if (blockNode.getPred(j).getMode().equals(Mode.getM())) {
								copy.setPred(j, loopPhi.getPred(1));
								lastMemNode = loopPhi.getPred(1);
							}
						} else if (inductions.containsKey(blockNode.getPred(j))) {
							Node induction = BackEdges.getOuts(blockNode).iterator().next().node;
							copy.setPred(j, blockNode);
							for (int k = 0; k < induction.getPredCount(); k++) {
								if (induction.getPred(k).equals(blockNode)) {
									induction.setPred(k, copy);
								}
							}
							inductions.put(blockNode, inductions.get(blockNode.getPred(j)));
						}
					}
				}
			}
			// adjust loop phi node
			for (int j = 0; j < loopPhi.getPredCount(); j++) {
				if (changedNodes.containsKey(loopPhi.getPred(j))) {
					loopPhi.setPred(j, changedNodes.get(loopPhi.getPred(j)));
				}
			}

			// clear maps for new unroll iteration
			Set<Node> nodes = blockNodes.get(block);
			nodes.clear();
			for (Node n : changedNodes.values()) {
				nodes.add(n);
			}
			changedNodes.clear();
			counter = count;
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

	private void copyBlockNodes(HashMap<Node, Node> changedNodes, Block block, Node node) {
		Graph graph = block.getGraph();
		// copy the whole block
		for (Node blockNode : blockNodes.get(block)) {
			if (!blockNode.equals(node)) {
				changedNodes.put(blockNode, graph.copyNode(blockNode));
			}
		}
	}

	private Const getIncrementConstantOrNull(Node node) {
		if (node.getPredCount() > 1 && isConstant(node.getPred(1)) && node instanceof Add) {
			return (Const) node.getPred(1);
		} else if (node.getPredCount() > 1 && isConstant(node.getPred(0)) && node instanceof Add) {
			return (Const) node.getPred(0);
		} else {
			return null;
		}
	}

	private Const getConstantCompareNodeOrNull(Node left, Node right) {
		if (isConstant(left) && inductionVariables.containsKey(right)) {
			return (Const) left;
		} else if (isConstant(right) && inductionVariables.containsKey(left)) {
			return (Const) right;
		} else {
			return null;
		}
	}

	private Const getStartingValueOrNull(Entry<Node, Node> entry) {
		// TODO: discover the starting value through other loops as well
		if (entry.getKey().getPred(0).equals(entry.getValue()) && isConstant(entry.getKey().getPred(1))) {
			return (Const) entry.getKey().getPred(1);
		} else if (entry.getKey().getPred(1).equals(entry.getValue()) && isConstant(entry.getKey().getPred(0))) {
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

	@Override
	public void visit(Start start) {
		/*
		 * if (finishedLoops.contains(start.getGraph())) { return; }
		 */
		if (finished)
			return;

		OptimizationUtils utils = new OptimizationUtils(start.getGraph());
		dominators = utils.getDominators();
		backedges = utils.getBackEdges();
		inductionVariables = utils.getInductionVariables();
		blockNodes = utils.getBlockNodes();
		loopPhis = utils.getLoopPhis();
	}
}
