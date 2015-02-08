package compiler.firm.optimization.visitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import firm.bindings.binding_irdom;
import firm.nodes.Anchor;
import firm.nodes.Bad;
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
		HashMap<Graph, EntityDetails> entityDetails = new HashMap<>();
		for (Entry<Entity, EntityDetails> entityDetail : programDetails.getEntityDetails().entrySet()) {

			Graph graph = entityDetail.getKey().getGraph();
			if (graph == null)
				continue;

			entityDetails.put(graph, entityDetail.getValue());
		}

		for (Entry<Graph, EntityDetails> entity : entityDetails.entrySet()) {
			HashMap<Node, Node> replacements = new HashMap<>();

			Graph graph = entity.getKey();
			EntityDetails entityDetail = entity.getValue();
			Set<Cmp> cmps = new HashSet<>();
			for (Entry<Node, BlockInformation> blockInformation : entityDetail.getBlockInformations().entrySet()) {
				if (blockInformation.getValue().getEndNode() instanceof Cmp) {
					cmps.add((Cmp) blockInformation.getValue().getEndNode());
				}
			}

			for (Cmp cmp : cmps) {
				binding_irdom.compute_postdoms(graph.ptr);
				binding_irdom.compute_doms(graph.ptr);
				BackEdges.enable(graph);

				checkAndUnrollLoop(cmp, programDetails, entityDetail, replacements);
				BackEdges.disable(graph);
			}

			compiler.firm.FirmUtils.replaceNodes(replacements);
			compiler.firm.FirmUtils.removeBadsAndUnreachable(graph);
			binding_irdom.compute_postdoms(graph.ptr);
			binding_irdom.compute_doms(graph.ptr);

		}
	}

	public static void checkAndUnrollLoop(Cmp cmp, ProgramDetails programDetails, EntityDetails entityDetails, HashMap<Node, Node> nodeReplacements) {
		Block loopHeader = (Block) cmp.getBlock();
		if (finishedLoops.contains(loopHeader))
			return;

		if (entityDetails == null || entityDetails.getBlockInformation(loopHeader) == null)
			return;

		LoopInfo loopInfo = FirmUtils.getLoopInfos(cmp);

		if (loopInfo == null)
			return;

		Graph graph = loopHeader.getGraph();

		int phiCount = getPhiCount(entityDetails, loopHeader);
		if (phiCount <= 2 && loopInfo.isOneBlockLoop()) {
			boolean sideEffects = false;
			for (Block block : FirmUtils.getLoopBlocks(loopInfo)) {
				if (!loopHeader.equals(block) && entityDetails.getBlockInformation(block).hasSideEffects()) {
					sideEffects = true;
				}
			}

			int value = (int) loopInfo.getStart() + (int) (loopInfo.getChange() * loopInfo.getCycleCount());
			// Just calculate result
			if (!sideEffects && replaceLoopIfPossible(entityDetails, loopInfo, value, loopInfo.getLastLoopBlock(), nodeReplacements)) {
				return;
			}

		}

		Node memoryPhi = entityDetails.getBlockInformation(loopHeader).getMemoryPhi();

		if (memoryPhi.getPredCount() > 2)
			return;

		// don't unroll too big blocks
		int nodeCount = 0;
		Set<Block> loopBlocks = FirmUtils.getLoopBlocks(loopInfo);
		for (Block loopBlock : loopBlocks) {
			nodeCount += entityDetails.getBlockInformation(loopBlock).getNumberOfNodes();
		}

		if (nodeCount > 50)
			return;

		if (loopHeader.getPredCount() != 2)
			return;

		// replace the increment operation

		int unrollFactor = 0;
		int correction = 0;
		if (loopInfo.getCycleCount() >= MAX_UNROLL_FACTOR) {
			unrollFactor = MAX_UNROLL_FACTOR;
		}
		correction = (int) (loopInfo.getCycleCount() % MAX_UNROLL_FACTOR);

		for (Block loopBlock : loopBlocks) {
			if (!loopBlock.equals(loopHeader) && FirmUtils.getLoopTailIfHeader(loopBlock) != null) {
				return; // This loop contains another loop
			}
		}

		Graph g = cmp.getGraph();

		// Append not unrolled content

		entityDetails = programDetails.getEntityDetails(g);

		for (int i = 0; i < correction; i++) {
			HashMap<Node, Node> replacements = new HashMap<>();
			unroll(entityDetails, loopInfo, 2, replacements, true);
			compiler.firm.FirmUtils.replaceNodes(replacements);
			compiler.firm.FirmUtils.removeBadsAndUnreachable(graph);
		}
		binding_irdom.compute_postdoms(graph.ptr);
		binding_irdom.compute_doms(graph.ptr);

		for (int i = 0; i < cmp.getPredCount(); i++) {
			if (cmp.getPred(i) instanceof Const) {
				Const temp = (Const) cmp.getPred(i);
				Node newConst = g.newConst((temp.getTarval().asInt() - correction * (int) loopInfo.getChange()), temp.getMode());
				cmp.setPred(i, newConst);
			}
		}

		for (int i = unrollFactor; i > 0 && i % 2 == 0; i = i / 2) {
			BackEdges.disable(g);
			programDetails.updateGraphs(Arrays.asList(g));
			BackEdges.enable(g);

			HashMap<Node, Node> replacements = new HashMap<>();
			entityDetails = programDetails.getEntityDetails(g);
			unroll(entityDetails, loopInfo, 2, replacements, false);
			compiler.firm.FirmUtils.replaceNodes(replacements);
			compiler.firm.FirmUtils.removeBadsAndUnreachable(graph);
			binding_irdom.compute_postdoms(graph.ptr);
			binding_irdom.compute_doms(graph.ptr);

			loopInfo.setLastLoopBlock(FirmUtils.getLoopTailIfHeader(loopHeader));
		}
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

		FirmUtils.removeKeepAlive(entityDetails.getBlockInformation(loopHeader).getMemoryPhi());
		// remove the control flow edge
		for (int i = 0; i < loopHeader.getPredCount(); i++) {
			loopHeader.setPred(i, block.getGraph().newBad(Mode.getX()));
		}
		FirmUtils.removeKeepAlive(loopHeader);

		finishedLoops.add(block);
		return true;
	}

	private static void unroll(EntityDetails entityDetails, LoopInfo loopInfo, int unrollFactor, HashMap<Node, Node> nodeReplacements,
			boolean afterLoop) {
		Block loopHeader = loopInfo.getLoopHeader();
		Graph graph = loopHeader.getGraph();

		Set<Block> loopBlocks = FirmUtils.getLoopBlocks(loopInfo);

		HashMap<Node, Node> blockPredecessors = new HashMap<>();
		HashMap<Node, Node> nodeMapping = new HashMap<>();

		Node[] dummyPredecessors = new Node[loopHeader.getPredCount()];
		for (int i = 0; i < loopHeader.getPredCount(); i++) {
			Node predecessor = loopHeader.getPred(i);
			Node dummyJump = graph.newBad(Mode.getX());
			dummyPredecessors[i] = dummyJump;

			if (loopBlocks.contains(predecessor.getBlock())) {
				blockPredecessors.put(predecessor, dummyJump);
			} else {
				nodeMapping.put(predecessor, dummyJump);
			}
		}

		Node memoryPhi = entityDetails.getBlockInformation(loopHeader).getMemoryPhi();
		for (int i = 0; i < memoryPhi.getPredCount(); i++) {
			if (!loopBlocks.contains(memoryPhi.getPred(i).getBlock())) {
				nodeMapping.put(memoryPhi.getPred(i), graph.newNoMem());
			}
		}

		// Add all nodes outside the loop to known predecessor. Avoid copy of them
		for (Block block : loopBlocks) {
			for (Node node : entityDetails.getBlockInformation(block).getNodes()) {
				for (Node predecessor : node.getPreds()) {
					if (!loopBlocks.contains(predecessor.getBlock())) {
						nodeMapping.put(predecessor, predecessor);
					}

				}
			}
		}

		Block newBlock = (Block) graph.newBlock(dummyPredecessors);

		nodeMapping.put(loopHeader, newBlock);
		nodeMapping.put(graph.getStartBlock(), graph.getStartBlock());

		graph.keepAlive(newBlock);

		InliningCopyGraphVisitor copyVisitor = new InliningCopyGraphVisitor(newBlock, new LinkedList<Node>(), nodeMapping, blockPredecessors);

		BlockFilterVisitor visitProxy = new BlockFilterVisitor(copyVisitor, loopBlocks);
		graph.walkPostorder(visitProxy);
		copyVisitor.cleanupNodes();

		graph.keepAlive(copyVisitor.getNodeMapping().get(loopInfo.getLastLoopBlock()));
		Proj oldProjJmp = (Proj) nodeMapping.get(loopInfo.getFirstLoopBlock()).getPred(0);

		if (afterLoop) {
			int loopHead = 0;
			int loopBody = 1;

			for (int i = 0; i < loopHeader.getPredCount(); i++) {
				if (loopBlocks.contains(loopHeader.getPred(i).getBlock())) {
					loopBody = i;
				} else {
					loopHead = i;
				}
			}

			Node oldPredJmp = loopHeader.getPred(loopHead);

			if (oldPredJmp instanceof Proj) {
				Node newJmp = graph.newProj(((Proj) oldPredJmp).getPred(), oldPredJmp.getMode(), ((Proj) oldPredJmp).getNum());
				Graph.exchange(nodeMapping.get(oldPredJmp), newJmp);
			} else {
				Node newJmp = graph.newJmp(oldPredJmp.getBlock());
				Graph.exchange(nodeMapping.get(oldPredJmp), newJmp);
			}
			Node loopEndBlock = loopHeader.getPred(loopBody).getBlock();

			Node successorLoop = graph.newJmp(nodeMapping.get(loopEndBlock));

			Graph.exchange(oldPredJmp, successorLoop);

			for (Phi phi : entityDetails.getBlockInformation(loopHeader).getPhis()) {
				nodeMapping.get(phi).setPred(loopHead, phi.getPred(loopHead));
				phi.setPred(loopHead, nodeMapping.get(phi.getPred(loopBody)));
			}

			Node unneccessaryJump = nodeMapping.get(loopHeader.getPred(loopBody));
			Graph.exchange(unneccessaryJump, FirmUtils.newBad(unneccessaryJump));

			// Kill old condition
			Node loopConditionProj = nodeMapping.get(loopInfo.getFirstLoopBlock().getPred(0));
			Graph.exchange(loopConditionProj, graph.newJmp(loopConditionProj.getBlock()));
		} else {
			for (Node predecessor : dummyPredecessors) {
				if (predecessor instanceof Bad) {
					Node jmp = graph.newJmp(loopInfo.getLastLoopBlock());
					Graph.exchange(predecessor, jmp);
					break;
				}
			}

			BlockInformation loopHeaderInformation = entityDetails.getBlockInformation(loopHeader);
			for (Phi phi : loopHeaderInformation.getPhis()) {
				Node newPhi = nodeMapping.get(phi);
				for (int j = 0; j < phi.getPredCount(); j++) {
					Node predecessor = phi.getPred(j);
					if (loopBlocks.contains(predecessor.getBlock())) {
						Node newPredecessor = nodeMapping.get(predecessor);
						nodeReplacements.put(newPhi, predecessor);
						phi.setPred(j, newPredecessor);
					} else {
						nodeMapping.get(phi).setPred(j, predecessor);
					}
				}
			}

			Node newJmp = graph.newJmp(nodeMapping.get(loopInfo.getLastLoopBlock()));

			Node endNode = entityDetails.getBlockInformation(loopInfo.getLastLoopBlock()).getEndNode();
			Graph.exchange(endNode, newJmp);
			Graph.exchange(nodeMapping.get(endNode), FirmUtils.newBad(nodeMapping.get(endNode)));

			FirmUtils.removeKeepAlive(oldProjJmp.getBlock());
			Graph.exchange(oldProjJmp, graph.newJmp(oldProjJmp.getBlock()));
		}

	}
}
