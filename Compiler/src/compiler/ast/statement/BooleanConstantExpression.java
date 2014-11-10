package compiler.ast.statement;

import compiler.ast.AstVisitor;
import compiler.lexer.Position;

public class BooleanConstantExpression extends Expression {
	private final boolean value;

	public BooleanConstantExpression(Position position, boolean value) {
		super(position);
		this.value = value;
	}

	public boolean isValue() {
		return value;
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
