package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;

public class NegateExpression extends UnaryExpression {

	public NegateExpression(Expression operand) {
		super(operand);
	}

}
