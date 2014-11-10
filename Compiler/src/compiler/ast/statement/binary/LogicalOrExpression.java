package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class LogicalOrExpression extends BinaryExpression {

	public LogicalOrExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
