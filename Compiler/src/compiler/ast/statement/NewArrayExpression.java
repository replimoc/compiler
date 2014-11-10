package compiler.ast.statement;

import compiler.ast.AstVisitor;
import compiler.ast.statement.type.Type;
import compiler.lexer.Position;

public class NewArrayExpression extends Expression {
	private final Type type;
	private final Expression firstDimension;

	public NewArrayExpression(Position position, Type type, Expression firstDimension) {
		super(position);
		this.type = type;
		this.firstDimension = firstDimension;
	}

	public Type getType() {
		return type;
	}

	public Expression getFirstDimension() {
		return firstDimension;
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}