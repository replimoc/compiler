package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class LogicalAndExpression extends BinaryExpression {

	public LogicalAndExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
