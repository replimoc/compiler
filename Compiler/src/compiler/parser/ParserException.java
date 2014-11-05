package compiler.parser;

import compiler.lexer.Token;

public class ParserException extends Exception {
	private static final long serialVersionUID = -5282537953189117934L;

	private final Token unexpectedToken;

	ParserException(Token t) {
		unexpectedToken = t;
	}

	@Override
	public String toString() {
		return "Line: " + unexpectedToken.getPosition().getLine()
				+ ". Unexpected token '" + unexpectedToken.getTokenString()
				+ "' at character: "
				+ unexpectedToken.getPosition().getCharacter();
	}

	@Override
	public String getMessage() {
		return toString();
	}
}