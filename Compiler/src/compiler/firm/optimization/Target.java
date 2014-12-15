package compiler.firm.optimization;

import firm.TargetValue;
import firm.nodes.Node;

/**
 * Represents a target value for a node.
 *
 */
public class Target {
	/**
	 * Target value
	 */
	private final TargetValue targetValue;
	private final Node node;

	/**
	 * Flag if the node shall be removed from the graph.
	 */
	private final boolean constant;

	public Target(TargetValue target, boolean constant) {
		this.targetValue = target;
		this.node = null;
		this.constant = constant;
	}

	public Target(Node target) {
		this.targetValue = null;
		this.node = target;
		this.constant = true;
	}

	public Target(TargetValue target) {
		this(target, false);
	}

	public TargetValue getTargetValue() {
		return targetValue;
	}

	public boolean isConstant() {
		return constant;
	}

	public boolean isNode() {
		return node != null;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (constant ? 1231 : 1237);
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((targetValue == null) ? 0 : targetValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Target other = (Target) obj;
		if (constant != other.constant)
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (targetValue == null) {
			if (other.targetValue != null)
				return false;
		} else if (!targetValue.equals(other.targetValue))
			return false;
		return true;
	}

}