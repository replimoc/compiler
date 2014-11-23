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

}
