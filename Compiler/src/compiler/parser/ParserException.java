package compiler.parser;

import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class ParserException extends Exception {
	private static final long serialVersionUID = -5282537953189117934L;

	private final Token unexpectedToken;
	private final TokenType expectedToken;

	ParserException(Token t, TokenType expected) {
		unexpectedToken = t;
		this.expectedToken = expected;
	}

	ParserException(Token t) {
		unexpectedToken = t;
		expectedToken = null;
	}

	public Token getUnexpectedToken() {
		return unexpectedToken;
	}

	@Override
	public String toString() {
		if (expectedToken != null) {
			return "Error in line: " + unexpectedToken.getPosition().getLine() + ". Unexpected token '" + unexpectedToken.getTokenString()
					+ "' at character: " + unexpectedToken.getPosition().getCharacter() + ". Expected token: '" + expectedToken.getString() + "'";
		} else {
			return "Error in line: " + unexpectedToken.getPosition().getLine() + ". Unexpected token '" + unexpectedToken.getTokenString()
					+ "' at character: " + unexpectedToken.getPosition().getCharacter();
		}
	}

	@Override
	public String getMessage() {
		return toString();
	}
}