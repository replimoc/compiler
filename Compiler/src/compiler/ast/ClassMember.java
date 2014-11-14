package compiler.ast;

import compiler.Symbol;
import compiler.lexer.Position;

public abstract class ClassMember extends AstNode {
	private final Symbol identifier;

	public ClassMember(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
