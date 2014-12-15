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
import firm.nodes.Node;
import firm.nodes.Proj;

public class ControlFlowVisitor extends OptimizationVisitor {

	public static OptimizationVisitorFactory getFactory() {
		return new OptimizationVisitorFactory() {
			@Override
			public OptimizationVisitor create() {
				return new ControlFlowVisitor();
			}
		};
	}

	private HashMap<Node, Node> nodeReplacements = new HashMap<>();

	@Override
	public HashMap<Node, Node> getNodeReplacements() {
		return nodeReplacements;
	}

	/**
	 * Control flow optimization
	 * 
	 * @author Valentin Zickner
	 */
	private TargetValue optimizeCompare(Cmp compare) {
		Node left = compare.getLeft();
		Node right = compare.getRight();

		if (left instanceof Const && right instanceof Const) {
			boolean result = false;
			boolean success = true;

			int leftInt = getInteger(((Const) left).getTarval());
			int rightInt = getInteger(((Const) right).getTarval());
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

	private int getInteger(TargetValue value) {
		String result = new StringBuilder(value.getBitpattern()).reverse().toString();
		String negative = result.substring(1);
		if (result.charAt(0) == '1') {
			return (int) -(Math.pow(2, negative.length()) - Math.abs(Integer.parseInt(negative, 2)));
		} else {
			return Integer.parseInt(result, 2);
		}
	}

	/**
	 * Control flow optimization
	 * 
	 * @author Valentin Zickner
	 */
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
					// targets.put(proj, jump);
					nodeReplacements.put(proj, jump);
				} else {
					nodeReplacements.put(proj, proj.getGraph().newBad(proj.getPred().getMode()));
				}
			}
		}
	}
}
