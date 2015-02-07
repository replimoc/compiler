package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.LoopInfo;
import compiler.firm.optimization.evaluation.BlockInformation;
import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.TargetValue;
import firm.bindings.binding_irdom;
import firm.nodes.Anchor;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.End;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;

public class LoopUnrolling {

	private static final Set<Block> finishedLoops = new HashSet<>();
	private static final int MAX_UNROLL_FACTOR = 8;

	public static void unrollLoops(ProgramDetails programDetails) {
		for (Entry<Entity, EntityDetails> entityDetail : programDetails.getEntityDetails().entrySet()) {
			HashMap<Node, Node> replacements = new HashMap<>();

			Graph graph = entityDetail.getKey().getGraph();
			if (graph == null)
				continue;

			BackEdges.enable(graph);
			for (Entry<Node, BlockInformation> blockInformation : entityDetail.getValue().getBlockInformations().entrySet()) {
				if (blockInformation.getValue().getEndNode() instanceof Cmp) {
					binding_irdom.compute_postdoms(graph.ptr);
					binding_irdom.compute_doms(graph.ptr);

					checkAndUnrollLoop((Cmp) blockInformation.getValue().getEndNode(), entityDetail.getValue(), replacements);
				}
			}
			BackEdges.disable(graph);
			compiler.firm.FirmUtils.replaceNodes(replacements);
			compiler.firm.FirmUtils.removeBadsAndUnreachable(graph);
			binding_irdom.compute_postdoms(graph.ptr);
			binding_irdom.compute_doms(graph.ptr);

		}
	}

	public static void checkAndUnrollLoop(Cmp cmp, EntityDetails entityDetails, HashMap<Node, Node> nodeReplacements) {
		Block loopHeader = (Block) cmp.getBlock();
		if (finishedLoops.contains(loopHeader))
			return;

		if (entityDetails == null || entityDetails.getBlockInformation(loopHeader) == null)
			return;

		LoopInfo loopInfo = FirmUtils.getLoopInfos(cmp);

		if (loopInfo == null)
			return;

		if (getPhiCount(entityDetails, loopHeader) > 2) // TODO: Remove this
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
			if (!isCalculatable(entityDetails, loopInfo.getLastLoopBlock())) {
				unrollFactor = MAX_UNROLL_FACTOR;
				while (unrollFactor > 1 && (target % unrollFactor) != 0) {
					unrollFactor -= 1;
				}
				if (unrollFactor < 2)
					return;
			} else {
				// calculate loop result
				long value = Integer.MIN_VALUE + loopInfo.getIncr().getTarval().asLong() - mod - 1;
				replaceLoopIfPossible(entityDetails, loopInfo, value, loopInfo.getLastLoopBlock(), nodeReplacements);
			}
		} else if (loopInfo.getCycleCount() == Integer.MIN_VALUE) {
			long count = (Integer.MIN_VALUE) - loopInfo.getStartingValue().getTarval().asLong();
			long mod = count % loopInfo.getIncr().getTarval().asLong();
			long target = (long) Math.ceil((double) count / loopInfo.getIncr().getTarval().asLong()) + (mod == 0 ? 1 : 0);
			if (!isCalculatable(entityDetails, loopInfo.getLastLoopBlock())) {
				unrollFactor = MAX_UNROLL_FACTOR;
				while (unrollFactor > 1 && (target % unrollFactor) != 0) {
					unrollFactor -= 1;
				}
				if (unrollFactor < 2)
					return;
			} else {
				// calculate loop result
				long value = Integer.MAX_VALUE + loopInfo.getIncr().getTarval().asLong() - mod + 1;
				replaceLoopIfPossible(entityDetails, loopInfo, value, loopInfo.getLastLoopBlock(), nodeReplacements);
			}
		} else if (isCalculatable(entityDetails, loopInfo.getLastLoopBlock())) {
			// calculate loop result
			long value = loopInfo.getStartingValue().getTarval().asLong()
					+ (Math.abs(loopInfo.getCycleCount()) * loopInfo.getIncr().getTarval().asLong());
			replaceLoopIfPossible(entityDetails, loopInfo, value, loopInfo.getLastLoopBlock(), nodeReplacements);
		} else if (Math.abs(loopInfo.getCycleCount()) < 2 || (Math.abs(loopInfo.getCycleCount()) % unrollFactor) != 0) {
			return;
		}

		if (unrollFactor < 2) // TODO: Remove this
			return;

		Node memoryPhi = entityDetails.getBlockInformation(loopHeader).getMemoryPhi();

		if (memoryPhi.getPredCount() > 2)
			return;

		// don't unroll too big blocks
		// TODO Do not only use loop tail, use all content blocks
		if (entityDetails.getBlockInformation(loopInfo.getLastLoopBlock()).getNumberOfNodes() > 30)
			return;

		// replace the increment operation
		nodeReplacements.put(loopInfo.getIncr(),
				graph.newConst(loopInfo.getIncr().getTarval().mul(new TargetValue(unrollFactor, loopInfo.getIncr().getMode()))));

		unroll(entityDetails, loopInfo, unrollFactor);
		finishedLoops.add(loopHeader);

	}

	private static int getPhiCount(EntityDetails entityDetails, Block block) {
		// count phi's in inside this block
		int count = 0;
		for (Node node : entityDetails.getBlockInformation(block).getNodes()) {
			if (node instanceof Phi) {
				count++;
			}
		}
		return count;
	}

	private static boolean isCalculatable(EntityDetails entityDetails, Block block) {
		return entityDetails.getBlockInformation(block).getNumberOfNodes() <= 2;
	}

	private static boolean replaceLoopIfPossible(EntityDetails entityDetails, LoopInfo loopInfo, long value, Block block,
			HashMap<Node, Node> nodeReplacements) {
		// collect nodes that need to be altered
		Node constNode = block.getGraph().newConst((int) value, FirmUtils.getModeInteger());
		Node loopHeader = block.getPred(0).getBlock();
		Node preLoopJmp = loopHeader.getPred(0);
		Cmp cmp = loopInfo.getCmp();

		// there should only be one condition node
		Node cond = BackEdges.getOuts(cmp).iterator().next().node;
		Node memoryPhi = entityDetails.getBlockInformation(loopHeader).getMemoryPhi(); // memory phi in loop header
		if (memoryPhi.getPredCount() > 2 || !memoryPhi.getPred(1).equals(memoryPhi))
			return false;
		Node memBeforeLoop = memoryPhi.getPred(0);

		Node loopCounter = loopInfo.getConditionalPhi();
		for (Edge e : BackEdges.getOuts(loopCounter)) {
			if (!e.node.equals(loopInfo.getArithmeticNode())) {
				for (int i = 0; i < e.node.getPredCount(); i++) {
					if (e.node.getPred(i).equals(loopCounter)) {
						// set constant predecessor for each successor of the loop counter
						e.node.setPred(i, constNode);
					}
				}
			}
		}

		// loop body has no memory node
		for (Edge e : BackEdges.getOuts(memoryPhi)) {
			if (!(e.node instanceof Anchor) && !(e.node instanceof End)) {
				for (int i = 0; i < e.node.getPredCount(); i++) {
					if (e.node.getPred(i).equals(memoryPhi)) {
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
			nodeReplacements.put(preLoopJmp, newJmp);
		}

		// remove the control flow edge
		loopHeader.setPred(0, block.getGraph().newBad(Mode.getX()));
		finishedLoops.add(block);
		return true;
	}

	private static void unroll(EntityDetails entityDetails, LoopInfo loopInfo, int unrollFactor) {
		HashMap<Node, Node> changedNodes = new HashMap<>();
		Block block = loopInfo.getLastLoopBlock();
		Const incr = loopInfo.getIncr();
		Node loopCounter = loopInfo.getConditionalPhi();
		Node arithmeticNode = loopInfo.getArithmeticNode();
		Node counter = loopInfo.getConditionalPhi();
		Graph graph = block.getGraph();
		Node loopPhi = entityDetails.getBlockInformation(loopInfo.getCmp().getBlock()).getMemoryPhi();
		Node firstMemNode = loopPhi;
		Node lastMemNode = loopPhi.getPred(1);
		HashMap<Node, Node> inductions = new HashMap<Node, Node>();
		inductions.put(loopInfo.getConditionalPhi(), loopInfo.getArithmeticNode());

		for (int i = 1; i < unrollFactor; i++) {
			// create the 'i + 1' increment node for the new iteration
			Node count = graph.newAdd(block, loopCounter,
					graph.newConst(incr.getTarval().mul(new TargetValue(i, incr.getMode()))), loopCounter.getMode());
			count.setBlock(block);

			copyBlockNodes(entityDetails, changedNodes, block, arithmeticNode);

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
			Set<Node> nodes = entityDetails.getBlockInformation(block).getNodes();
			nodes.clear();
			for (Node n : changedNodes.values()) {
				nodes.add(n);
			}
			changedNodes.clear();
			counter = count;
		}
	}

	private static void copyBlockNodes(EntityDetails entityDetails, HashMap<Node, Node> changedNodes, Block block, Node node) {
		Graph graph = block.getGraph();
		// copy the whole block
		for (Node blockNode : entityDetails.getBlockInformation(block).getNodes()) {
			if (!blockNode.equals(node)) {
				changedNodes.put(blockNode, graph.copyNode(blockNode));
			}
		}
	}
}
