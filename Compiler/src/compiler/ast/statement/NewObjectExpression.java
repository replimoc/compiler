package compiler.ast.statement;

import compiler.Symbol;

public class NewObjectExpression extends Expression {

	private final Symbol identifier;
	private final Expression[] parameters;

	public NewObjectExpression(Symbol identifier, Expression[] parameters) {
		this.identifier = identifier;
		this.parameters = parameters;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Expression[] getParameters() {
		return parameters;
	}

}
