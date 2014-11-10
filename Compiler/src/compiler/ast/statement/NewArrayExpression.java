package compiler.ast.statement;

import compiler.ast.statement.type.Type;

public class NewArrayExpression extends Expression {
	private final Type type;
	private final Expression firstDimension;

	public NewArrayExpression(Type type, Expression firstDimension) {
		this.type = type;
		this.firstDimension = firstDimension;
	}

	public Type getType() {
		return type;
	}

	public Expression getFirstDimension() {
		return firstDimension;
	}
}