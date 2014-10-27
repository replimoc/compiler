package compiler.lexer;

/**
 * This enum defines the basic types of tokens that can be handled by the lexer.
 * 
 * @author Valentin Zickner
 * @author Andreas Eberle
 *
 */
public enum TokenType {
	ERROR("error", false),
	IDENTIFIER("identifier", false),
	INTEGER("integer", false),
	EOF("EOF", false),
	EQUAL("=", false),
	ABSTRACT("abstract", true);

	private final String string;
	private final boolean keyword;

	TokenType(String string, boolean keyword) {
		this.string = string;
		this.keyword = keyword;
	}

	public String getString() {
		return string;
	}

	public boolean isKeyword() {
		return keyword;
	}
}
