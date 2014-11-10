package compiler.ast.statement;

import compiler.Symbol;
import compiler.lexer.Position;

public class VariableAccessExpression extends Expression {
	private final Symbol identifier;

	public VariableAccessExpression(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
