package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;

public class LogicalNotExpression extends UnaryExpression {

	public LogicalNotExpression(Expression operand) {
		super(operand);
	}

}
