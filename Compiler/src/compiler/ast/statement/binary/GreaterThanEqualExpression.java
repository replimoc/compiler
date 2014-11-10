package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class GreaterThanEqualExpression extends BinaryExpression {

	public GreaterThanEqualExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
