package compiler.ast.statement;

import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class BooleanConstantExpression extends PrimaryExpression {
	private final boolean value;

	public BooleanConstantExpression(Position position, boolean value) {
		super(position);
		this.value = value;
	}

	public boolean isValue() {
		return value;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
