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
	ABSTRACT("abstract", true),

    NOT_EQUAL("!=", false),
    NOT("!", false),
    LEFT_BRACKET("(", false),
    RIGHT_BRACKET(")", false),
    MUL_ASSIGN("*=", false),
    MUL("*", false),
    INCREMENT("++", false),
    ADD_ASSIGN("+=", false),
    ADD("+", false),
    COMA(",", false),
    SUB_ASSIGN("-=", false),
    DECREMENT("--", false),
    SUB("-", false),
    POINT(".", false),
    DIV_ASSIGN("/=", false),
    DIV("/", false),
    COLON(":", false),
    SEMI_COLON(";", false),
    SIGNED_LEFT_SHIFT_ASSIGN("<<=", false), // they are called signed and unsigned shifts here:
    SIGNED_LEFT_SHIFT("<<", false), // http://docs.oracle.com/javase/tutorial/java/nutsandbolts/op3.html
    LESS_EQUAL("<=", false),
    LESS("<", false),
    EQUAL_EQUAL("==", false),
//    EQUAL("=", false),
    GREATER_EQUAL(">=", false),
    SIGNED_RIGHT_SHIFT_ASSIGN(">>=", false),
    UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>=", false),
    UNSIGNED_RIGHT_SHIFT(">>>", false),
    SIGNED_RIGHT_SHIFT(">>", false),
    GREATER(">", false),
    QUESTION("?", false),
    MOD_ASSIGN("%=", false),
    MOD("%", false),
    AND_ASSIGN("&=", false),
    BINARY_AND("&&", false),
    BITWISE_AND("&", false),
    LEFT_SQUARE_BRACKET("[", false),
    RIGHT_SQUARE_BRACKET("]", false),
    BITWISE_XOR_ASSIGN("^=", false),
    BITWISE_XOR("^", false),
    LEFT_CURLY_BRACKET("{", false),
    RIGHT_CURLY_BRACKET("}", false),
    BITWISE_NOT("~", false),
    BITWISE_OR_ASSIGN("|=", false),
    OR("||", false),
    BITWISE_OR("|", false),
    ;

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
