package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.LoopInfo;

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
	public void visit(Block loopHeader) {
		if (finishedLoops.contains(loopHeader))
			return;

		if (utils == null) {
			return; // Start block
		}

		Cmp cmp = compares.get(loopHeader);

		if (cmp == null)
			return; // Not a loop

		LoopInfo loopInfo = FirmUtils.getLoopInfos(cmp);

		if (loopInfo == null)
			return;

		if (getPhiCount(loopHeader) > 2) // TODO: Remove this
			return;

		if (!loopInfo.isOneBlockLoop()) // TODO: Remove this
			return;
		// TODO: Check if it is the innermost loop!

		int unrollFactor = MAX_UNROLL_FACTOR;
		while (unrollFactor > 1 && (Math.abs(loopInfo.getCycleCount()) % unrollFactor) != 0) {
			unrollFactor -= 1;
		}

		Graph graph = loopHeader.getGraph();
		// unroll if block generates overflow
		if (loopInfo.getCycleCount() == Integer.MAX_VALUE) {
			long count = (Integer.MAX_VALUE) - loopInfo.getStartingValue().getTarval().asLong();
			long mod = count % loopInfo.getIncr().getTarval().asLong();
			long target = (long) Math.ceil((double) (count) / loopInfo.getIncr().getTarval().asLong() + (mod == 0 ? 1 : 0));
			if (!isCalculatable(loopInfo.getLastLoopBlock())) {
				unrollFactor = MAX_UNROLL_FACTOR;
				while (unrollFactor > 1 && (target % unrollFactor) != 0) {
					unrollFactor -= 1;
				}
				if (unrollFactor < 2)
					return;
			} else {
				// calculate loop result
				long value = Integer.MIN_VALUE + loopInfo.getIncr().getTarval().asLong() - mod - 1;
				replaceLoopIfPossible(loopInfo, value, loopInfo.getLastLoopBlock());
			}
		} else if (loopInfo.getCycleCount() == Integer.MIN_VALUE) {
			long count = (Integer.MIN_VALUE) - loopInfo.getStartingValue().getTarval().asLong();
			long mod = count % loopInfo.getIncr().getTarval().asLong();
			long target = (long) Math.ceil((double) count / loopInfo.getIncr().getTarval().asLong()) + (mod == 0 ? 1 : 0);
			if (!isCalculatable(loopInfo.getLastLoopBlock())) {
				unrollFactor = MAX_UNROLL_FACTOR;
				while (unrollFactor > 1 && (target % unrollFactor) != 0) {
					unrollFactor -= 1;
				}
				if (unrollFactor < 2)
					return;
			} else {
				// calculate loop result
				long value = Integer.MAX_VALUE + loopInfo.getIncr().getTarval().asLong() - mod + 1;
				replaceLoopIfPossible(loopInfo, value, loopInfo.getLastLoopBlock());
			}
		} else if (isCalculatable(loopInfo.getLastLoopBlock())) {
			// calculate loop result
			long value = loopInfo.getStartingValue().getTarval().asLong()
					+ (Math.abs(loopInfo.getCycleCount()) * loopInfo.getIncr().getTarval().asLong());
			replaceLoopIfPossible(loopInfo, value, loopInfo.getLastLoopBlock());
		} else if (Math.abs(loopInfo.getCycleCount()) < 2 || (Math.abs(loopInfo.getCycleCount()) % unrollFactor) != 0) {
			return;
		}

		if (unrollFactor < 2) // TODO: Remove this
			return;

		// counter
		HashMap<Node, Node> changedNodes = new HashMap<>();
		Node loopPhi = loopPhis.get(loopHeader);

		if (loopPhi.getPredCount() > 2)
			return;

		// don't unroll too big blocks
		if (blockNodes.get(loopInfo.getLastLoopBlock()).size() > 30) // TODO Do not only use loop tail, use all content blocks
			return;

		// replace the increment operation
		addReplacement(loopInfo.getIncr(),
				graph.newConst(loopInfo.getIncr().getTarval().mul(new TargetValue(unrollFactor, loopInfo.getIncr().getMode()))));

		unroll(loopInfo, changedNodes, loopPhi, unrollFactor);
		finishedLoops.add(loopHeader);

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

	private boolean isCalculatable(Block block) {
		return blockNodes.containsKey(block) && !(blockNodes.get(block).size() > 2);
	}

	private boolean replaceLoopIfPossible(LoopInfo loopInfo, long value, Block block) {
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

		Node loopCounter = loopInfo.getLoopCounter();
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
		if (preLoopJmp instanceof Proj && preLoopJmp.getMode().equals(Mode.getX())) {
			// this loop is inside another loop
			afterLoopBlock.setPred(0, preLoopJmp);
		} else {
			Node newJmp = block.getGraph().newJmp(preLoopJmp.getBlock());
			afterLoopBlock.setPred(0, newJmp);
			addReplacement(preLoopJmp, newJmp);
		}

		// remove the control flow edge
		loopHeader.setPred(0, block.getGraph().newBad(Mode.getX()));
		finishedLoops.add(block);
		return true;
	}

	private void unroll(LoopInfo loopInfo, HashMap<Node, Node> changedNodes, Node loopPhi,
			int unrollFactor) {
		Block block = loopInfo.getLastLoopBlock();
		Const incr = loopInfo.getIncr();
		Node loopCounter = loopInfo.getLoopCounter();
		Node arithmeticNode = loopInfo.getArithmeticNode();
		Node counter = loopInfo.getLoopCounter();
		Graph graph = block.getGraph();
		Node firstMemNode = loopPhi;
		Node lastMemNode = loopPhi.getPred(1);
		HashMap<Node, Node> inductions = new HashMap<Node, Node>();
		for (Entry<Node, Node> entry : inductionVariables.entrySet()) {
			if (entry.getKey().getBlock().equals(loopCounter.getBlock())) {
				// only all induction variables of this loop
				inductions.put(entry.getKey(), entry.getValue());
			}
		}

		for (int i = 1; i < unrollFactor; i++) {
			// create the 'i + 1' increment node for the new iteration
			Node count = graph.newAdd(block, loopCounter,
					graph.newConst(incr.getTarval().mul(new TargetValue(i, incr.getMode()))), loopCounter.getMode());
			count.setBlock(block);

			copyBlockNodes(changedNodes, block, arithmeticNode);

			// adjust all predecessors
			for (Entry<Node, Node> nodeEntry : changedNodes.entrySet()) {
				if (!nodeEntry.getKey().equals(arithmeticNode)) {
					Node blockNode = nodeEntry.getKey();
					Node copy = nodeEntry.getValue();

					// check dependencies for unrolled nodes
					for (int j = 0; j < blockNode.getPredCount(); j++) {
						if (blockNode.getPred(j).equals(counter)) {
							// node is before the loop increment
							copy.setPred(j, count);
						} else if (blockNode.getPred(j).equals(arithmeticNode)) {
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
							// FIXME: to support more than 2 phis in the loop header
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
