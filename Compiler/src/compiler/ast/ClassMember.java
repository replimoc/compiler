package compiler.ast;

import compiler.Symbol;
import compiler.lexer.Position;

public abstract class ClassMember extends Declaration implements Comparable<ClassMember> {
	private final Symbol identifier;

	public ClassMember(Position position, Symbol identifier) {
		super(position, identifier);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public int compareTo(ClassMember o) {
		int priorityOrder = this.getSortPriority() - o.getSortPriority();
		if (priorityOrder == 0) {
			return this.identifier.getValue().compareTo(o.identifier.getValue());
		} else {
			return priorityOrder;
		}
	}

	protected abstract int getSortPriority();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassMember other = (ClassMember) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}
