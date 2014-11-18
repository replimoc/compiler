package compiler.ast.nameanalysis;

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
	public boolean equals(Object obj) {
		Scope scopeObj = (Scope) obj;
		return scopeObj != null && scopeObj.parentScope == this.parentScope && scopeObj.oldChangeStackSize == this.oldChangeStackSize;
	}
}
