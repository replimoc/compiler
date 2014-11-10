package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public class UnaryExpression extends Expression {
	private final Expression operand;

	public UnaryExpression(Position position, Expression operand) {
		super(position);
		this.operand = operand;
	}

	public Expression getOperand() {
		return operand;
	}
}
