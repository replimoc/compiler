package compiler.ast.statement;


public class IntegerConstantExpression extends Expression {
	private final String literal;

	public IntegerConstantExpression(String integerLiteral) {
		this.literal = integerLiteral;
	}

	public String getIntegerLiteral() {
		return literal;
	}
}
