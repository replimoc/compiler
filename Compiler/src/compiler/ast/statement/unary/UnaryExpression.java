package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public abstract class UnaryExpression extends Expression {
	private final Expression operand;

	protected UnaryExpression(Position position, Expression operand) {
		super(position);
		this.operand = operand;
	}

	protected UnaryExpression(Position position) {
		super(position);
		this.operand = null;
	}

	public Expression getOperand() {
		return operand;
	}
}
