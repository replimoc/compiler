package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class LessThanExpression extends BinaryExpression {

	public LessThanExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
