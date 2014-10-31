package compiler.lexer;

import compiler.Symbol;

public class Token {
	private final TokenType type;
	private final Symbol symbol;
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
		this.symbol = null;
	}

	/**
	 * Constructor. Sets the type of the token to the given {@link TokenType} and the position of the token to the given {@link Position}. Sets the
	 * symbol of the token to the given symbol.
	 * 
	 * @param type
	 * @param position
	 * @param symbol
	 */
	public Token(TokenType type, Position position, Symbol symbol) {
		this.type = type;
		this.position = position;
		this.symbol = symbol;
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
	 * Return the symbol of the token.
	 * 
	 * @return
	 */
	public Symbol getSymbol() {
		return symbol;
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
			return "identifier " + symbol;
		case INTEGER:
			return "integer literal " + symbol;
		case ERROR:
			return type.getString() + " " + symbol;
		default:
			return type.getString();
		}
	}

	@Override
	public String toString() {
		return position + ": " + getTokenString();
	}
}
