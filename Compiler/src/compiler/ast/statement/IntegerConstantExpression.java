package compiler.ast.statement;

import compiler.ast.AstVisitor;
import compiler.lexer.Position;

public class IntegerConstantExpression extends Expression {
	private final String literal;

	public IntegerConstantExpression(Position position, String integerLiteral) {
		super(position);
		this.literal = integerLiteral;
	}

	public String getIntegerLiteral() {
		return literal;
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
