package compiler.ast;

import compiler.Symbol;
import compiler.lexer.Position;

public abstract class ClassMember extends AstNode implements Comparable<ClassMember> {
	private final Symbol identifier;

	public ClassMember(Position position, Symbol identifier) {
		super(position);
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
}
