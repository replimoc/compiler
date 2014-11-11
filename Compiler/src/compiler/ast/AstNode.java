package compiler.ast;

import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public abstract class AstNode {

	private final Position position;

	public AstNode(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	public abstract void accept(AstVisitor visitor);
}
