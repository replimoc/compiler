package compiler.firm.optimization.visitor;

import java.util.HashMap;
import java.util.HashSet;
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

	private Set<Cond> conditions = new HashSet<>();

	private OptimizationUtils optimizationUtils;

	public LoopFusionVisitor(ProgramDetails programDetails) {
		this.programDetails = programDetails;
	}

	@Override
	public void init(Graph graph) {
		super.init(graph);
		optimizationUtils = new OptimizationUtils(graph);
		optimizationUtils.getInductionVariables();
		optimizationUtils.getBlockNodes();
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return new HashMap<>();
	}

	@Override
	public void visit(Start start) {
	}

	@Override
	public void visit(Cond condition) {
		if (optimizationUtils == null)
			return;

		if (conditions.contains(condition))
			return;

		HashMap<Node, Node> backedges = optimizationUtils.getBackEdges();
		Block block = (Block) condition.getBlock();
		Block loopTail = FirmUtils.getLoopTailIfHeader(block);

		if (loopTail != null) {
			LoopHeader continueInfo = getLoopContinueBlocks(condition);

			if (continueInfo == null || continueInfo.loopContentBlock == null)
				return;

			LoopInfo loopInfo1 = calculateLoopInfo(condition);

			Node block2 = continueInfo.continueBlock;
			if (loopInfo1 != null && backedges.containsValue(block2)) { // Next is also a loop
				HashMap<Block, Set<Node>> blockNodes = optimizationUtils.getBlockNodes();
				Set<Node> nodes = blockNodes.get(block2);
				Node condition2 = getLeavingNode(nodes);

				if (!(condition2 instanceof Cond)) {
					return;
				}

				LoopHeader continueInfo2 = getLoopContinueBlocks(condition2);
				if (continueInfo2 == null)
					return;

				LoopInfo loopInfo2 = calculateLoopInfo(condition2);

				EntityDetails entityDetails = programDetails.getEntityDetails(graph);

				// TODO: Prove if loop header has side effects.
				if (loopInfo2 != null && loopInfo1.cycleCount == loopInfo2.cycleCount &&
						entityDetails.getBlockInformation(block).getSideEffects() <= 1 &&
						!entityDetails.getBlockInformation(continueInfo.loopContentBlock).hasSideEffects() &&
						entityDetails.getBlockInformation(block2).getSideEffects() <= 1 &&
						!entityDetails.getBlockInformation(continueInfo2.loopContentBlock).hasSideEffects()) {
					conditions.add(condition);

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

					// Correct mode m between loop bodys
					Node lastModeM1 = entityDetails.getBlockInformation(continueInfo.loopContentBlock).getLastModeM();
					BlockInformation loopBlockInfo2 = entityDetails.getBlockInformation(continueInfo2.loopContentBlock);
					Node firstModeM2 = loopBlockInfo2.getFirstModeM();
					Node lastModeM2 = loopBlockInfo2.getLastModeM();
					if (firstModeM2 == null) {
						lastModeM2 = lastModeM1;
					} else {
						firstModeM2.setPred(0, lastModeM1);
					}

					// Correct phi in first loop head
					if (lastModeM1 != lastModeM2) {
						for (Node node : blockNodes.get(block)) {
							if (node.getMode().equals(Mode.getM()) && node instanceof Phi) {
								for (int i = 0; i < node.getPredCount(); i++) {
									if (node.getPred(i).equals(lastModeM1)) {
										node.setPred(i, lastModeM2);
									}
								}
							}
						}
					}

					// Correct mode m in block after loops
					Node loopHeaderModeM1 = entityDetails.getBlockInformation(block).getLastModeM();
					Node loopHeaderModeM2 = entityDetails.getBlockInformation(block2).getLastModeM();

					for (Edge edge : BackEdges.getOuts(loopHeaderModeM2)) {
						Node continueModeM = edge.node;
						for (int i = 0; i < continueModeM.getPredCount(); i++) {
							if (continueModeM.getPred(i).equals(loopHeaderModeM2)) {
								continueModeM.setPred(i, loopHeaderModeM1);
							}
						}
					}

					addReplacement(loopHeaderModeM2, FirmUtils.newBad(loopHeaderModeM2));

					int phiPredecessor = -1;
					for (int i = 0; i < block2.getPredCount(); i++) {
						if (block2.getPred(i).getBlock().equals(continueInfo2.loopContentBlock)) {
							phiPredecessor = i;
						}
					}

					// Move nodes of second loop head in to first loop head
					for (Node node : blockNodes.get(block2)) {
						if (node instanceof Phi) {
							Node predecessor = getNextPredecessorWithOtherBlock(node, phiPredecessor);
							if (!predecessor.getBlock().equals(continueInfo2.loopContentBlock)) {
								addReplacement(node, predecessor);
								continue;
							}
						}
						node.setBlock(block);
					}

					// Remove second loop head
					addReplacement(block2, FirmUtils.newBad(block2));
				}
			}
		}
	}

	private Node getNextPredecessorWithOtherBlock(Node node, int predNum) {
		Node block = node.getBlock();
		while (node.getBlock().equals(block) && node instanceof Phi) {
			node = node.getPred(predNum);
		}
		return node;
	}

	private LoopInfo calculateLoopInfo(Node condition) {
		LoopInfo loopInfo = OptimizationUtils.getLoopInfos((Cmp) condition.getPred(0));
		if (loopInfo == null || !loopInfo.isOneBlockLoop())
			return null;
		else
			return loopInfo;
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
			if (BackEdges.getNOuts(proj) == 0)
				return null;

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
