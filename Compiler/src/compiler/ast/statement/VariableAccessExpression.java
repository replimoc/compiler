package compiler.ast.statement;

import compiler.Symbol;

public class VariableAccessExpression extends Expression {
	private final Symbol identifier;

	public VariableAccessExpression(Symbol identifier) {
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
