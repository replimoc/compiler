package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class InvalidMethodCallException extends SemanticAnalysisException {
	private static final long serialVersionUID = -1109058515492292496L;

	private final Symbol method;
	private final Position position;

	public InvalidMethodCallException(Symbol method, Position position) {
		super(position, buildMessage(method, position));
		this.method = method;
		this.position = position;
	}

	public Symbol getMethodIdentifier() {
		return method;
	}

	public Position getMemberPosition() {
		return position;
	}

	public static String buildMessage(Symbol method, Position position) {
		return "error: Invalid member function call " + method + " at position " + position;
	}
}
