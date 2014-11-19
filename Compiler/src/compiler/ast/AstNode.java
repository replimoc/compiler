package compiler.ast;

import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public abstract class AstNode {

	private final Position position;
	private Type type;

	public AstNode(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	public abstract void accept(AstVisitor visitor);

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
