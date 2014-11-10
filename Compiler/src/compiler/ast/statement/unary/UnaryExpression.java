package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;

public class UnaryExpression extends Expression {
	private final Expression operand;

	public UnaryExpression(Expression operand) {
		this.operand = operand;
	}

	public Expression getOperand() {
		return operand;
	}
}
