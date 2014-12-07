package compiler.ast.statement;

import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class IntegerConstantExpression extends PrimaryExpression {
	private final String literal;
	private final boolean negative;

	public IntegerConstantExpression(Position position, String integerLiteral) {
		this(position, integerLiteral, false);
	}

	public IntegerConstantExpression(Position pos, String literal, boolean negative) {
		super(pos);
		this.literal = literal;
		this.negative = negative;
	}

	public String getIntegerLiteral() {
		return negative ? "-" + literal : literal;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public boolean isNegative() {
		return negative;
	}
}
