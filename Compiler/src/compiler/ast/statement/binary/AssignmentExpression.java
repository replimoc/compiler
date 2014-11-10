package compiler.ast.statement.binary;

import compiler.ast.statement.Expression;

public class AssignmentExpression extends BinaryExpression {

	public AssignmentExpression(Expression operand1, Expression operand2) {
		super(operand1, operand2);
	}

}
