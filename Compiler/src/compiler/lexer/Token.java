package compiler.lexer;

import compiler.Symbol;

public class Token {
	private final TokenType type;
	private final Symbol value;
	private final Position position;

	public Token(TokenType type, Position position) {
		this.type = type;
		this.position = position;
		this.value = null;
	}

	public Token(TokenType type, Position position, Symbol value) {
		this.type = type;
		this.position = position;
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}

	public Symbol getValue() {
		return value;
	}

	public Position getPosition() {
		return position;
	}

	public String getTokenString() {
		switch (type) {
		case IDENTIFIER:
		case INTEGER:
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
