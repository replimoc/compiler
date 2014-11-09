package compiler.parser;

import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class ParserException extends Exception {
	private static final long serialVersionUID = -5282537953189117934L;

	private final Token invalidToken;
	private final TokenType expectedTokenType;

	protected ParserException(Token invalidToken, TokenType expectedTokenType) {
		this.invalidToken = invalidToken;
		this.expectedTokenType = expectedTokenType;
	}

	protected ParserException(Token invalidToken) {
		this(invalidToken, null);
	}

	public TokenType getExpectedTokenType() {
		return expectedTokenType;
	}

	public Token getInvalidToken() {
		return invalidToken;
	}

	@Override
	public String toString() {
		if (expectedTokenType != null) {
			return "Error in line: " + invalidToken.getPosition().getLine() + ". Unexpected token '" + invalidToken.getTokenString()
					+ "' at character: " + invalidToken.getPosition().getCharacter() + ". Expected token: '" + expectedTokenType.getString() + "'";
		} else {
			return "Error in line: " + invalidToken.getPosition().getLine() + ". Unexpected token '" + invalidToken.getTokenString()
					+ "' at character: " + invalidToken.getPosition().getCharacter();
		}
	}

	@Override
	public String getMessage() {
		return toString();
	}

}