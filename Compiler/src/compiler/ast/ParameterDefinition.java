package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
import compiler.lexer.Position;

public class ParameterDefinition extends AstNode {

	private final Type type;
	private final Symbol identifier;

	public ParameterDefinition(Position position, Type type, Symbol identifier) {
		super(position);
		this.type = type;
		this.identifier = identifier;

	}

	public Type getType() {
		return type;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
