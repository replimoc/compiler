package compiler.firm.optimization;

import firm.TargetValue;

/**
 * Represents a target value for a node.
 *
 */
public class Target {
	/**
	 * Target value
	 */
	private final TargetValue targetValue;

	/**
	 * Flag if the node already reached a fixpoint.
	 */
	private final boolean fixpointReached;

	public Target(TargetValue target, boolean fixpointReached) {
		this.targetValue = target;
		this.fixpointReached = fixpointReached;
	}

	public Target(TargetValue target) {
		this(target, false);
	}

	public TargetValue getTargetValue() {
		return targetValue;
	}

	public boolean isFixpointReached() {
		return fixpointReached;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fixpointReached ? 1231 : 1237);
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
		if (fixpointReached != other.fixpointReached)
			return false;
		if (targetValue == null) {
			if (other.targetValue != null)
				return false;
		} else if (!targetValue.equals(other.targetValue))
			return false;
		return true;
	}

}