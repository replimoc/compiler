package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class GreaterThanExpression extends BinaryExpression {

	public GreaterThanExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
