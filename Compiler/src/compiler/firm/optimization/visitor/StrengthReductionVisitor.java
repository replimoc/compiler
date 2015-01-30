package compiler.firm.optimization.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.nodes.Block;
import firm.nodes.Const;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Start;

public class StrengthReductionVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new StrengthReductionVisitor();
		}
	};

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private HashMap<Node, Node> inductionVariables = new HashMap<>();

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

		ArrayList<Block> sameLevelLoops = new ArrayList<>();
		L1: for (Block b : loops) {
			if (dominators.containsKey(b) && dominators.get(b).containsAll(loops)) {
				for (Map.Entry<Node, Node> entry : backedges.entrySet()) {
					if (entry.getValue().equals(b)) {
						if (dominators.containsKey((Block) entry.getKey()) && !dominators.get((Block) entry.getKey()).contains(block)
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

	@Override
	public void visit(Mul mul) {
		Node left = mul.getLeft();
		Node right = mul.getRight();

		if (inductionVariables.containsKey(left)) {
			if (dominators.get((Block) mul.getBlock()).contains(right.getBlock()) && !mul.getBlock().equals(right.getBlock())) {
				// right block dominates this mul -> right comes before the loop header
				if (left.getPred(0) != null && isConstant(left.getPred(0))) {
					for (Edge suc : BackEdges.getOuts(mul)) {
						// do not optimize if it references itself
						if (left.equals(suc.node))
							return;
					}
					Node pred = getInnerMostLoopHeader((Block) mul.getBlock());
					if (pred == null)
						return;
					Node preLoopBlock = pred.getPred(0).getBlock();
					if (!dominators.get(preLoopBlock).contains(right.getBlock())) {
						return;
					}
					Node base = mul.getGraph().newMul(preLoopBlock, left.getPred(0), right, mul.getMode());
					Node incr = inductionVariables.get(left);
					Node constant = null;
					if (incr.getPred(0) != null && !incr.getPred(0).equals(left)) {
						constant = incr.getPred(0);
					} else if (incr.getPred(1) != null && !incr.getPred(1).equals(left)) {
						constant = incr.getPred(1);
					} else {
						return;
					}
					if (!(constant instanceof Const) || !(right instanceof Const)) {
						return;
					}
					Node constNode = mul.getGraph().newConst(((Const) constant).getTarval().mul(((Const) right).getTarval()));
					Node dummy = mul.getGraph().newDummy(mul.getMode());
					Node loopPhi = mul.getGraph().newPhi(pred, new Node[] { base, dummy }, mul.getMode());
					Node add = mul.getGraph().newAdd(mul.getBlock(), loopPhi, constNode, mul.getMode());
					loopPhi.setPred(1, add);
					addReplacement(mul, loopPhi);
				}
			}
		} else if (inductionVariables.containsKey(right) && !right.equals(mul)) {
			if (dominators.get((Block) mul.getBlock()).contains(left.getBlock()) && !mul.getBlock().equals(left.getBlock())) {
				// left block dominates this mul -> left comes before the loop header
				if (right.getPred(0) != null && isConstant(right.getPred(0))) {
					for (Edge suc : BackEdges.getOuts(mul)) {
						// do not optimize if it references itself
						if (right.equals(suc.node))
							return;
					}
					Node pred = getInnerMostLoopHeader((Block) mul.getBlock());
					if (pred == null)
						return;
					Node preLoopBlock = pred.getPred(0).getBlock();
					if (!dominators.get(preLoopBlock).contains(left.getBlock())) {
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
					Node constNode = mul.getGraph().newConst(((Const) constant).getTarval().mul(((Const) left).getTarval()));
					Node dummy = mul.getGraph().newDummy(mul.getMode());
					Node loopPhi = mul.getGraph().newPhi(pred, new Node[] { base, dummy }, mul.getMode());
					Node add = mul.getGraph().newAdd(mul.getBlock(), loopPhi, constNode, mul.getMode());
					loopPhi.setPred(1, add);
					addReplacement(mul, loopPhi);
				}
			}
		}
	}

	@Override
	public void visit(Start start) {
		FirmUtils utils = new FirmUtils(start.getGraph());
		dominators = utils.getDominators();
		backedges = utils.getBackEdges();
		inductionVariables = utils.getInductionVariables();
	}
}
