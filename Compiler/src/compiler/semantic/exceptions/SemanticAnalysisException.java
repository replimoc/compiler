package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class SemanticAnalysisException extends Exception {
	private static final long serialVersionUID = -1003909587273441949L;
	private final Position position;

	public SemanticAnalysisException(Position position) {
		this(position, null);
	}

	public SemanticAnalysisException(Position position, String message) {
		super(message);
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
