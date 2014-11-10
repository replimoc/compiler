package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class NonEqualityExpression extends BinaryExpression {

	public NonEqualityExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
