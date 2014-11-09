package compiler.parser;

import compiler.lexer.Position;
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
		String expectedTokenTypeString = expectedTokenType == null ? "unknown" : expectedTokenType.getString();
		Position position = invalidToken == null ? null : invalidToken.getPosition();
		int line = position == null ? -1 : position.getLine();
		int character = position == null ? -1 : position.getCharacter();
		String tokenString = invalidToken == null ? "unknown" : invalidToken.getTokenString();

		return "Error in line: " + line + ". Unexpected token '" + tokenString + "' at character: " + character + ". Expected token: '"
				+ expectedTokenTypeString + "'";
	}

	@Override
	public String getMessage() {
		return toString();
	}

}