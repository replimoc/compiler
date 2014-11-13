package compiler.ast.statement.unary;

import compiler.ast.statement.Expression;
import compiler.lexer.Position;

public abstract class PostfixExpression extends UnaryExpression {

	/**
	 * Constructor with expression.
	 * 
	 * @param position
	 * @param operand
	 */
	protected PostfixExpression(Position position, Expression operand) {
		super(position, operand);
	}

	/**
	 * Constructor for primary expressions.
	 * 
	 * @param position
	 */
	protected PostfixExpression(Position position) {
		super(position);
	}

}
