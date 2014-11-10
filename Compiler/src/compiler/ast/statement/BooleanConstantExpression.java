package compiler.ast.statement;

public class BooleanConstantExpression extends Expression {
	private final boolean value;

	public BooleanConstantExpression(boolean value) {
		this.value = value;
	}

	public boolean isValue() {
		return value;
	}

}
