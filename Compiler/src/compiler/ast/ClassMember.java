package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
import compiler.lexer.Position;

public class ClassMember extends AstNode {
	private final Symbol identifier;
	private final Type type;

	public ClassMember(Position position, Symbol identifier, Type type) {
		super(position);
		this.identifier = identifier;
		this.type = type;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
	
	public Type getType() {
		return type;
	}
}
