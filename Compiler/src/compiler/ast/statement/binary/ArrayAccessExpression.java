package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class ArrayAccessExpression extends BinaryExpression {

	public ArrayAccessExpression(Position position, Expression arrayExpression, Expression indexExpression) {
		super(position, arrayExpression, indexExpression);
	}

	public Expression getArrayExpression() {
		return super.getOperand1();
	}

	public Expression getIndexExpression() {
		return super.getOperand2();
	}

}
