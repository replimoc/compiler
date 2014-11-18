package compiler.semantic;

import compiler.lexer.Position;

public class TypeErrorException extends Exception {
	private static final long serialVersionUID = -271485782975658532L;

	private Position position;

	public TypeErrorException(Position position) {
		this.position = position;
	}

	public String toString() {
		String positionString = "unknown";
		if (position != null) {
			positionString = position.toString();
		}
		return "error: Type error on position " + positionString;
	}
}
