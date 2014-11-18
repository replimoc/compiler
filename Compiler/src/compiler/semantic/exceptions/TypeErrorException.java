package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class TypeErrorException extends Exception {
	private static final long serialVersionUID = -271485782975658532L;

	private final Position position;

	public TypeErrorException(Position position) {
		this(position, null);
	}

	public TypeErrorException(Position position, String message) {
		super(message);
		this.position = position;
	}

	@Override
	public String toString() {
		String positionString = position != null ? position.toString() : "unknown";
		String messageString = super.getMessage() != null ? ": " + super.getMessage() : "";
		return "error: Type error at position " + positionString + messageString;
	}

	@Override
	public String getMessage() {
		return toString();
	}
}
