package compiler.ast.statement;

import compiler.lexer.Position;

public class IntegerConstantExpression extends Expression {
	private final String literal;

	public IntegerConstantExpression(Position position, String integerLiteral) {
		super(position);
		this.literal = integerLiteral;
	}

	public String getIntegerLiteral() {
		return literal;
	}
}
