package compiler.ast.declaration;

import compiler.Symbol;
import compiler.lexer.Position;

public abstract class MemberDeclaration extends Declaration implements Comparable<MemberDeclaration> {

	private final boolean isStatic;

	public MemberDeclaration(Position position, boolean isStatic, Symbol identifier) {
		super(position, identifier);
		this.isStatic = isStatic;
	}

	@Override
	public int compareTo(MemberDeclaration o) {
		int priorityOrder = this.getSortPriority() - o.getSortPriority();
		if (priorityOrder == 0) {
			return super.identifier.compareTo(o.identifier);
		} else {
			return priorityOrder;
		}
	}

	protected abstract int getSortPriority();

	public boolean isStatic() {
		return isStatic;
	}
}
