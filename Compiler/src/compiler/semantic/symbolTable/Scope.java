package compiler.semantic.symbolTable;

public class Scope {

	private final Scope parentScope;
	private final int oldChangeStackSize;

	public Scope(Scope parentScope, int oldChangeStackSize) {
		this.parentScope = parentScope;
		this.oldChangeStackSize = oldChangeStackSize;
	}

	public Scope getParentScope() {
		return parentScope;
	}

	public int getOldChangeStackSize() {
		return oldChangeStackSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + oldChangeStackSize;
		result = prime * result + ((parentScope == null) ? 0 : parentScope.hashCode());
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
		Scope other = (Scope) obj;
		if (oldChangeStackSize != other.oldChangeStackSize)
			return false;
		if (parentScope == null) {
			if (other.parentScope != null)
				return false;
		} else if (!parentScope.equals(other.parentScope))
			return false;
		return true;
	}

}
