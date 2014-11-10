package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;

public class ReturnStatement extends UnaryExpression {

	public ReturnStatement(Expression operand) {
		super(operand);
	}

}
