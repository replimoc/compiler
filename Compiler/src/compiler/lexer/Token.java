package compiler.lexer;

public class Token {
	private final TokenType type;
	private final String value;
	private final Position position;

	public Token(TokenType type, Position position) {
		this.type = type;
		this.position = position;
		this.value = null;
	}

	public Token(TokenType type, Position position, String value) {
		this.type = type;
		this.position = position;
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public Position getPosition() {
		return position;
	}
}
