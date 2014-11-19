package compiler.ast.statement.unary;

import compiler.lexer.Position;

public abstract class PrimaryExpression extends PostfixExpression {

	protected PrimaryExpression(Position position) {
		super(position);
	}

}
