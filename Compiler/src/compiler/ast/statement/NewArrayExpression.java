package compiler.ast.statement;

import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NewArrayExpression extends PrimaryExpression {
	private final Type type;
	private final Expression firstDimension;

	public NewArrayExpression(Position position, Type type, Expression firstDimension) {
		super(position);
		this.type = type;
		this.firstDimension = firstDimension;
	}

	@Override
	public Type getType() {
		return type;
	}

	public Expression getFirstDimension() {
		return firstDimension;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}