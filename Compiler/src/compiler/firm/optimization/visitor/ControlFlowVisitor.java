package compiler.firm.optimization.visitor;

import java.util.HashMap;

import compiler.firm.FirmUtils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.TargetValue;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Const;
import firm.nodes.Jmp;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;

/**
 * Control flow optimization
 * 
 * @author Valentin Zickner
 */
public class ControlFlowVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY = new OptimizationVisitorFactory<Node>() {
		@Override
		public OptimizationVisitor<Node> create() {
			return new ControlFlowVisitor();
		}
	};

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return getNodeReplacements();
	}

	private TargetValue optimizeCompare(Cmp compare) {
		Node left = compare.getLeft();
		Node right = compare.getRight();

		if (isConstant(left) && isConstant(right)) {
			boolean result = false;
			boolean success = true;

			int leftInt = ((Const) left).getTarval().asInt();
			int rightInt = ((Const) right).getTarval().asInt();
			switch (compare.getRelation()) {
			case Equal:
				result = leftInt == rightInt;
				break;
			case LessGreater:
				result = leftInt != rightInt;
				break;
			case Less:
				result = leftInt < rightInt;
				break;
			case Greater:
				result = leftInt > rightInt;
				break;
			case LessEqual:
				result = leftInt <= rightInt;
				break;
			case GreaterEqual:
				result = leftInt >= rightInt;
				break;
			default:
				success = false;
				break;
			}
			TargetValue target = TargetValue.getBad();
			if (success) {
				target = result ? TargetValue.getBTrue() : TargetValue.getBFalse();
			}
			return target;
		} else {
			return TargetValue.getBad();
		}

	}

	@Override
	public void visit(Cond condition) {
		boolean eliminate = true;
		boolean useCase = false;

		for (Node pred : condition.getPreds()) {
			if (pred instanceof Cmp) {
				TargetValue targetValue = optimizeCompare((Cmp) pred);
				eliminate &= targetValue.isConstant();
				useCase = targetValue.isOne();
			}
		}

		if (eliminate) {
			for (Edge edge : BackEdges.getOuts(condition)) {

				Proj proj = (Proj) edge.node;

				if ((proj.getNum() == FirmUtils.TRUE) == useCase) {
					Block block = (Block) condition.getBlock();
					Node jump = block.getGraph().newJmp(block);
					addReplacement(proj, jump);
				} else {
					addReplacement(proj, proj.getGraph().newBad(proj.getPred().getMode()));
				}
			}
		}
	}

	@Override
	public void visit(Block block) {
		if (block.getPredCount() == 1 && block.getPred(0) instanceof Jmp) {
			addReplacement(block, block.getPred(0).getBlock());
		}
	}

	@Override
	public void visit(Phi phi) {
		boolean isTrivial = false;
		Node otherNode = null;
		for (Node predecessor : phi.getPreds()) {
			if (predecessor.equals(phi)) {
				isTrivial = true;
			} else {
				otherNode = predecessor;
			}
		}

		if (isTrivial && phi.getLoop() == 0) {
			addReplacement(phi, otherNode);
		}
	}
}
