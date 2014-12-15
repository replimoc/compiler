package compiler.firm.optimization;

import compiler.firm.FirmUtils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Mode;
import firm.TargetValue;
import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.NodeVisitor;
import firm.nodes.Proj;

public class ControlFlowVisitor extends OptimizationVisitor implements NodeVisitor {

	/**
	 * Control flow optimization
	 * 
	 * @author Valentin Zickner
	 */
	@Override
	public void visit(Cmp compare) {
		Node left = compare.getLeft();
		Node right = compare.getRight();

		TargetValue leftTargetValue = getTargetValue(left);
		TargetValue rightTargetValue = getTargetValue(right);

		if (leftTargetValue.isConstant() && rightTargetValue.isConstant()) {
			boolean result = false;
			boolean success = true;
			int leftInt = getInteger(leftTargetValue);
			int rightInt = getInteger(rightTargetValue);
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
			setTargetValue(compare, target);
		} else {
			setTargetValue(compare, TargetValue.getBad());
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
			eliminate &= getTargetValue(pred).isConstant();
			useCase = getTargetValue(pred).isOne();
		}

		if (eliminate) {
			for (Edge edge : BackEdges.getOuts(condition)) {

				Proj proj = (Proj) edge.node;

				if ((proj.getNum() == FirmUtils.TRUE) == useCase) {
					Block block = (Block) condition.getBlock();
					Node jump = block.getGraph().newJmp(block);
					// targets.put(proj, jump);
					targets.put(proj, new Target(jump));
				} else {
					// targets.put(proj, proj.getGraph().newBad(proj.getPred().getMode()));
					targets.put(proj, new Target(proj.getGraph().newBad(proj.getPred().getMode())));
				}
			}
		}
	}

	@Override
	public void visit(Const constant) {
		if (constant.getMode().equals(Mode.getIs()) || constant.getMode().equals(Mode.getBu()) || constant.getMode().equals(Mode.getLu())) {
			setTargetValue(constant, constant.getTarval(), true);
		}
	}

}
