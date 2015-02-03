package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.Mode;
import firm.TargetValue;
import firm.nodes.Anchor;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.End;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Start;

public class UnrollingVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new UnrollingVisitor();
		}
	};

	private static final Set<Block> finishedLoops = new HashSet<>();

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Node, Node> inductionVariables = new HashMap<>();
	private final HashMap<Block, Cmp> compares = new HashMap<>();
	private HashMap<Block, Set<Node>> blockNodes = new HashMap<>();
	private HashMap<Block, Phi> loopPhis = new HashMap<>();
	private static final int MAX_UNROLL_FACTOR = 8;
	private OptimizationUtils utils;

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	@Override
	public void visit(Cmp cmp) {
		if (!compares.containsKey(cmp.getBlock())) {
			compares.put((Block) cmp.getBlock(), cmp);
			cmp.getBlock().accept(this);
		}
	}

	@Override
	public void visit(Block block) {
		if (finishedLoops.contains(block))
			return;

		if (utils == null) {
			return; // Start block
		}

		Set<OptimizationUtils.LoopInfo> loopInfos = utils.getLoopInfos(block, compares);
		if (loopInfos == null)
			return;

		for (OptimizationUtils.LoopInfo loopInfo : loopInfos) {
			// negative cycle count means the counter is descending
			if (!(loopInfo.cycleCount == Integer.MIN_VALUE) && Math.abs(loopInfo.cycleCount) < 2)
				return;

			int unrollFactor = MAX_UNROLL_FACTOR;
			while (unrollFactor > 1 && (Math.abs(loopInfo.cycleCount) % unrollFactor) != 0) {
				unrollFactor -= 1;
			}

			Graph graph = block.getGraph();
			// unroll if block generates overflow
			if (loopInfo.cycleCount == Integer.MAX_VALUE) {
				long count = (Integer.MAX_VALUE) - loopInfo.startingValue.getTarval().asLong();
				long mod = count % loopInfo.incr.getTarval().asLong();
				long target = (long) Math.ceil((double) (count) / loopInfo.incr.getTarval().asLong() + (mod == 0 ? 1 : 0));
				if (blockNodes.get(block).size() > 2) {
					unrollFactor = MAX_UNROLL_FACTOR;
					while (unrollFactor > 1 && (target % unrollFactor) != 0) {
						unrollFactor -= 1;
					}
					if (unrollFactor < 2)
						return;
				} else {
					// calculate loop result
					long value = Integer.MIN_VALUE + loopInfo.incr.getTarval().asLong() - mod - 1;
					replaceLoopIfPossible(loopInfo, value, block);
				}
			} else if (loopInfo.cycleCount == Integer.MIN_VALUE) {
				long count = (Integer.MIN_VALUE) - loopInfo.startingValue.getTarval().asLong();
				long mod = count % loopInfo.incr.getTarval().asLong();
				long target = (long) Math.ceil((double) count / loopInfo.incr.getTarval().asLong()) + (mod == 0 ? 1 : 0);
				if (blockNodes.get(block).size() > 2) {
					unrollFactor = MAX_UNROLL_FACTOR;
					while (unrollFactor > 1 && (target % unrollFactor) != 0) {
						unrollFactor -= 1;
					}
					if (unrollFactor < 2)
						return;
				} else {
					// calculate loop result
					long value = Integer.MAX_VALUE + loopInfo.incr.getTarval().asLong() - mod + 1;
					replaceLoopIfPossible(loopInfo, value, block);
				}
			} else if (unrollFactor < 2 || (Math.abs(loopInfo.cycleCount) % unrollFactor) != 0) {
				return;
			}

			// counter
			HashMap<Node, Node> changedNodes = new HashMap<>();
			Node loopPhi = loopPhis.get(backedges.get(block));

			if (loopPhi.getPredCount() > 2)
				return;

			// don't unroll too big blocks
			if (blockNodes.get(block).size() > 30)
				return;

			// replace the increment operation
			addReplacement(loopInfo.incr, graph.newConst(loopInfo.incr.getTarval().mul(new TargetValue(unrollFactor, loopInfo.incr.getMode()))));

			unroll(block, loopInfo.incr, loopInfo.loopCounter, loopInfo.node, changedNodes, loopInfo.loopCounter, loopPhi, unrollFactor);

			finishedLoops.add(block);

		}
	}

	private boolean replaceLoopIfPossible(OptimizationUtils.LoopInfo loopInfo, long value, Block block) {
		// collect nodes that need to be altered
		Node constNode = block.getGraph().newConst((int) value, FirmUtils.getModeInteger());
		Node loopHeader = block.getPred(0).getBlock();
		Node preLoopJmp = loopHeader.getPred(0);
		Cmp cmp = compares.get(loopHeader);
		if (cmp == null || !backedges.containsValue(loopHeader))
			return false;

		// there should only be one condition node
		Node cond = BackEdges.getOuts(cmp).iterator().next().node;
		Node loopPhi = loopPhis.get(loopHeader); // memory phi in loop header
		if (loopPhi.getPredCount() > 2 || !loopPhi.getPred(1).equals(loopPhi))
			return false;
		Node memBeforeLoop = loopPhi.getPred(0);

		Node loopCounter = loopInfo.loopCounter;
		for (Edge e : BackEdges.getOuts(loopCounter)) {
			if (!e.node.equals(inductionVariables.get(loopCounter))) {
				for (int i = 0; i < e.node.getPredCount(); i++) {
					if (e.node.getPred(i).equals(loopCounter)) {
						// set constant predecessor for each successor of the loop counter
						e.node.setPred(i, constNode);
					}
				}
			}
		}

		// loop body has no memory node
		for (Edge e : BackEdges.getOuts(loopPhi)) {
			if (!(e.node instanceof Anchor) && !(e.node instanceof End)) {
				for (int i = 0; i < e.node.getPredCount(); i++) {
					if (e.node.getPred(i).equals(loopPhi)) {
						// alter memory flow
						e.node.setPred(i, memBeforeLoop);
					}
				}
			}
		}

		Node afterLoopBlock = null;
		for (Edge edge : BackEdges.getOuts(cond)) {
			Proj proj = (Proj) edge.node;
			if (proj.getNum() == FirmUtils.FALSE) {
				// there is always a false proj with exactly one successor
				afterLoopBlock = BackEdges.getOuts(proj).iterator().next().node;
			}
		}

		// replace nodes
		Node newJmp = block.getGraph().newJmp(preLoopJmp.getBlock());
		afterLoopBlock.setPred(0, newJmp);

		// remove the control flow edge
		loopHeader.setPred(0, block.getGraph().newBad(Mode.getX()));
		addReplacement(preLoopJmp, newJmp);
		finishedLoops.add(block);
		return true;
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

	private void copyBlockNodes(HashMap<Node, Node> changedNodes, Block block, Node node) {
		Graph graph = block.getGraph();
		// copy the whole block
		for (Node blockNode : blockNodes.get(block)) {
			if (!blockNode.equals(node)) {
				changedNodes.put(blockNode, graph.copyNode(blockNode));
			}
		}
	}

	@Override
	public void visit(Start start) {
		utils = new OptimizationUtils(start.getGraph());
		backedges = utils.getBackEdges();
		inductionVariables = utils.getInductionVariables();
		blockNodes = utils.getBlockNodes();
		loopPhis = utils.getLoopPhis();
	}
}
