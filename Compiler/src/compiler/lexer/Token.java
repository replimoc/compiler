package compiler.lexer;

import compiler.Symbol;

public class Token {
	private final TokenType type;
	private final Symbol value;
	private final Position position;

	/**
	 * Constructor. Sets the type of the token to the given {@link TokenType} and the position of the token to the given {@link Position}.
	 * 
	 * @param type
	 * @param position
	 */
	public Token(TokenType type, Position position) {
		this.type = type;
		this.position = position;
		this.value = null;
	}

	/**
	 * Constructor. Sets the type of the token to the given {@link TokenType} and the position of the token to the given {@link Position}. Sets the
	 * value of the token to the given value.
	 * 
	 * @param type
	 * @param position
	 * @param value
	 */
	public Token(TokenType type, Position position, Symbol value) {
		this.type = type;
		this.position = position;
		this.value = value;
	}

	/**
	 * Return the type of the token.
	 * 
	 * @return
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Return the value of the token.
	 * 
	 * @return
	 */
	public Symbol getSymbol() {
		return value;
	}

	/**
	 * Return the position of the token.
	 * 
	 * @return
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Return the token string value.
	 * 
	 * @return
	 */
	public String getTokenString() {
		// to comply with the --lextest settings
		switch (type) {
		case IDENTIFIER:
			return "identifier " + value;
		case INTEGER:
			return "integer literal " + value;
		case ERROR:
			return type.getString() + " " + value;
		default:
			return type.getString();
		}
	}

	@Override
	public String toString() {
		return position + ": " + getTokenString();
	}
}
