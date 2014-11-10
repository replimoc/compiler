package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class ArrayAccessExpression extends BinaryExpression {

	public ArrayAccessExpression(Expression arrayExpression, Expression indexExpression) {
		super(arrayExpression, indexExpression);
	}

	public Expression getArrayExpression() {
		return super.getOperand1();
	}

	public Expression getIndexExpression() {
		return super.getOperand2();
	}

}
