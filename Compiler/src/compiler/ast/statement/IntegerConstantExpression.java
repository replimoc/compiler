package compiler.ast.statement;

import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class IntegerConstantExpression extends PrimaryExpression {
	private final String literal;

	public IntegerConstantExpression(Position position, String integerLiteral) {
		super(position);
		this.literal = integerLiteral;
	}

	public String getIntegerLiteral() {
		return literal;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
