package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.optimization.evaluation.BlockInformation;
import compiler.firm.optimization.evaluation.EntityDetails;
import compiler.firm.optimization.evaluation.ProgramDetails;
import compiler.firm.optimization.visitor.OptimizationUtils.LoopInfo;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Graph;
import firm.Mode;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Start;

public class LoopFusionVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY(final ProgramDetails programDetails) {
		return new OptimizationVisitorFactory<Node>() {
			@Override
			public OptimizationVisitor<Node> create() {
				return new LoopFusionVisitor(programDetails);
			}
		};
	}

	private final ProgramDetails programDetails;

	private static boolean LOCK = false;

	private OptimizationUtils optimizationUtils;

	public LoopFusionVisitor(ProgramDetails programDetails) {
		this.programDetails = programDetails;
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return new HashMap<>();
	}

	@Override
	public void visit(Start start) {
		optimizationUtils = new OptimizationUtils(start.getGraph());
		optimizationUtils.getInductionVariables(); // TODO: Move this to OptimizationUtils: Calculate induction variables
		optimizationUtils.getBlockNodes();
	}

	@Override
	public void visit(Cond condition) {
		if (optimizationUtils == null)
			return;

		if (LOCK)
			return;

		HashMap<Node, Node> backedges = optimizationUtils.getBackEdges();
		Node block = condition.getBlock();
		Graph graph = condition.getGraph();

		if (backedges.containsValue(block)) {
			LoopHeader continueInfo = getLoopContinueBlocks(condition);

			Node content1 = continueInfo.loopContentBlock;
			LoopInfo loopInfo1 = calculateLoopInfo(block, content1, condition);

			Node block2 = continueInfo.continueBlock;
			if (backedges.containsValue(block2)) { // Next is also a loop
				HashMap<Block, Set<Node>> blockNodes = optimizationUtils.getBlockNodes();
				Set<Node> nodes = blockNodes.get(block2);
				Node condition2 = getLeavingNode(nodes);

				LoopHeader continueInfo2 = getLoopContinueBlocks(condition2);
				LoopInfo loopInfo2 = calculateLoopInfo(block2, continueInfo2.loopContentBlock, condition2);
				Node content2 = continueInfo2.loopContentBlock;

				if (loopInfo1.cycleCount == loopInfo2.cycleCount) {
					LOCK = true;

					// Set continue proj to continue after second loop
					Proj oldProj = continueInfo.continueProj;
					Node newProj = graph.newProj(condition, oldProj.getMode(), oldProj.getNum());
					newProj.setBlock(oldProj.getBlock());
					addReplacement(continueInfo2.continueProj, newProj);

					// Set second loop proj to jump in first content
					Node newJmp = graph.newJmp(continueInfo.loopContentBlock);
					addReplacement(continueInfo2.loopContentProj, newJmp);

					// Correct jump to loop head
					Node oldJmp = getLeavingNode(blockNodes.get(continueInfo.loopContentBlock));
					Node newJmpToLoopHead = graph.newJmp(continueInfo2.loopContentBlock);
					addReplacement(oldJmp, newJmpToLoopHead);

					EntityDetails entityDetails = programDetails.getEntityDetails(graph);

					// Correct mode m between loop bodys
					Node lastModeM1 = entityDetails.getBlockInformation(continueInfo.loopContentBlock).getLastModeM();
					BlockInformation loopBlockInfo2 = entityDetails.getBlockInformation(continueInfo2.loopContentBlock);
					Node firstModeM2 = loopBlockInfo2.getFirstModeM();
					Node lastModeM2 = loopBlockInfo2.getLastModeM();
					firstModeM2.setPred(0, lastModeM1);

					// Correct phi in first loop head
					for (Node node : blockNodes.get(block)) {
						if (node.getMode().equals(Mode.getM()) && node instanceof Phi) {
							for (int i = 0; i < node.getPredCount(); i++) {
								if (node.getPred(i).equals(lastModeM1)) {
									node.setPred(i, lastModeM2);
								}
							}
						}
					}

					// Correct mode m in block after loops
					Node loopHeaderModeM1 = entityDetails.getBlockInformation(block).getLastModeM();
					Node loopHeaderModeM2 = entityDetails.getBlockInformation(block2).getLastModeM();

					Node continueModeM = entityDetails.getBlockInformation(continueInfo2.continueBlock).getFirstModeM();
					for (int i = 0; i < continueModeM.getPredCount(); i++) {
						if (continueModeM.getPred(i).equals(loopHeaderModeM2)) {
							continueModeM.setPred(i, loopHeaderModeM1);
						}
					}

					addReplacement(loopHeaderModeM2, FirmUtils.newBad(loopHeaderModeM2));

					// Move nodes of second loop head in to first loop head
					for (Node node : blockNodes.get(block2)) {
						node.setBlock(block);
					}

					// Remove second loop head
					addReplacement(block2, FirmUtils.newBad(block2));

					block2.getGraph().keepAlive(continueInfo2.loopContentBlock);
				}
			}
		}
	}

	private LoopInfo calculateLoopInfo(Node block, Node loopBlock, Node condition) {

		HashMap<Block, Cmp> compares = new HashMap<>();
		compares.put((Block) block, (Cmp) condition.getPred(0));

		Set<LoopInfo> loopInfos = optimizationUtils.getLoopInfos((Block) loopBlock, compares);

		if (loopInfos != null && loopInfos.size() > 0) {
			return loopInfos.iterator().next();
		}
		return null;
	}

	private Node getLeavingNode(Set<Node> nodes) {
		Node result = null;
		for (Node node : nodes) {
			if (node instanceof Cond || node instanceof Jmp)
				result = node;
		}
		return result;
	}

	private LoopHeader getLoopContinueBlocks(Node condition) {
		Node continueBlock = null;
		Proj continueProj = null;

		Node loopContentBlock = null;
		Proj loopContentProj = null;

		for (Edge projEdge : BackEdges.getOuts(condition)) {
			Proj proj = (Proj) projEdge.node;
			Node successorBlock = FirmUtils.getFirstSuccessor(proj);

			if (FirmUtils.blockPostdominates(condition.getBlock(), successorBlock)) {
				loopContentBlock = successorBlock;
				loopContentProj = proj;
			} else {
				continueBlock = successorBlock;
				continueProj = proj;
			}
		}
		return new LoopHeader(continueBlock, continueProj, loopContentBlock, loopContentProj);
	}

	private class LoopHeader {
		public final Node continueBlock;
		public final Proj continueProj;
		public final Node loopContentBlock;
		public final Proj loopContentProj;

		public LoopHeader(Node continueBlock, Proj continueProj, Node loopContentBlock, Proj loopContentProj) {
			this.continueBlock = continueBlock;
			this.continueProj = continueProj;
			this.loopContentBlock = loopContentBlock;
			this.loopContentProj = loopContentProj;
		}
	}
}
