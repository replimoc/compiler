package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class EqualityExpression extends BinaryExpression {

	public EqualityExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
