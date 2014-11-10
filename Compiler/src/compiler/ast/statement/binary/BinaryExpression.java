package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class BinaryExpression {
	private final Expression operand1;
	private final Expression operand2;

	public BinaryExpression(Expression operand1, Expression operand2) {
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	public Expression getOperand1() {
		return operand1;
	}

	public Expression getOperand2() {
		return operand2;
	}

}
