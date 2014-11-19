package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class TypeErrorException extends SemanticAnalysisException {
	private static final long serialVersionUID = -271485782975658532L;

	public TypeErrorException(Position position) {
		this(position, null);
	}

	public TypeErrorException(Position position, String message) {
		super(position, buildMessage(position, message));
	}

	public static String buildMessage(Position position, String message) {
		String positionString = position != null ? position.toString() : "unknown";
		String messageString = message != null ? ": " + message : "";
		return "error: type error at position " + positionString + messageString;
	}
}
