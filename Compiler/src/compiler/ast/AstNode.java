package compiler.ast;

import compiler.lexer.Position;

public class AstNode {

	private final Position position;

	public AstNode(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}
}
