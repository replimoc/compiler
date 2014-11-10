package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class LessThanEqualExpression extends BinaryExpression {

	public LessThanEqualExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
