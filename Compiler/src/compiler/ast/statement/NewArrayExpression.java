package compiler.ast.statement;

import compiler.ast.statement.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NewArrayExpression extends Expression {
	private final Type type;
	private final Expression firstDimension;
	private final int dimensions;

	public NewArrayExpression(Position position, Type type, Expression firstDimension, int dimensions) {
		super(position);
		this.type = type;
		this.firstDimension = firstDimension;
		this.dimensions = dimensions;
	}

	public Type getType() {
		return type;
	}

	public Expression getFirstDimension() {
		return firstDimension;
	}

	public int getDimensions() {
		return dimensions;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}