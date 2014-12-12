package compiler.ast.declaration;

import compiler.Symbol;
import compiler.lexer.Position;

public abstract class MemberDeclaration extends Declaration implements Comparable<MemberDeclaration> {

	public MemberDeclaration(Position position, Symbol identifier) {
		super(position, identifier);
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
}
