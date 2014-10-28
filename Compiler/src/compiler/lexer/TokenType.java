package compiler.lexer;

/**
 * This enum defines the basic types of tokens that can be handled by the lexer.
 * 
 * @author Valentin Zickner
 * @author Andreas Eberle
 *
 */
public enum TokenType {

	/* special tokens */
	ERROR("error"),
	EOF("EOF"),
	IDENTIFIER("identifier"),
	INTEGER("integer"),

	/* key words */
	ABSTRACT("abstract", true),
	ASSERT("assert", true),
	BOOLEAN("boolean", true),
	BREAK("break", true),
	BYTE("byte", true),
	CASE("case", true),
	CATCH("catch", true),
	CHAR("char", true),
	CLASS("class", true),
	CONST("const", true),
	CONTINUE("continue", true),
	DEFAULT("default", true),
	DOUBLE("double", true),
	DO("do", true),
	ELSE("else", true),
	ENUM("enum", true),
	EXTENDS("extends", true),
	FALSE("false", true),
	FINALLY("finally", true),
	FINAL("final", true),
	FLOAT("float", true),
	FOR("for", true),
	GOTO("goto", true),
	IF("if", true),
	IMPLEMENTS("implements", true),
	IMPORT("import", true),
	INSTANCEOF("instanceof", true),
	INTERFACE("interface", true),
	INT("int", true),
	LONG("long", true),
	NATIVE("native", true),
	NEW("new", true),
	NULL("null", true),
	PACKAGE("package", true),
	PRIVATE("private", true),
	PROTECTED("protected", true),
	PUBLIC("public", true),
	RETURN("return", true),
	SHORT("short", true),
	STATIC("static", true),
	STRICTFP("strictfp", true),
	SUPER("super", true),
	SWITCH("switch", true),
	SYNCHRONIZED("synchronized", true),
	THIS("this", true),
	THROWS("throws", true),
	THROW("throw", true),
	TRANSIENT("transient", true),
	TRUE("true", true),
	TRY("try", true),
	VOID("void", true),
	VOLATILE("volatile", true),
	WHILE("while", true),

	/* operators */
	/** Not equal: != */
	NOTEQUAL("!="),
	/** Logical not: ! */
	LOGICALNOT("!"),
	/** Left parenthesis: ( */
	LP("("),
	/** Right parenthesis: ) */
	RP(")"),
	/** Multiply and assign: *= */
	MULTIPLYASSIGN("*="),
	/** Multiply: * */
	MULTIPLY("*"),
	/** Increment: ++ */
	INCREMENT("++"),
	/** Add and assign: += */
	ADDASSIGN("+="),
	/** Add: + */
	ADD("+"),
	/** Comma: , */
	COMMA(","),
	/** Subtract and assign: -= */
	SUBTRACTASSIGN("-="),
	/** Decrement: -- */
	DECREMENT("--"),
	/** Subtract: - */
	SUBTRACT("-"),
	/** Point: . */
	POINT("."),
	/** Divide and assign: /= */
	DIVIDEASSIGN("/="),
	/** Divide: / */
	DIVIDE("/"),
	/** Colon: : */
	COLON(":"),
	/** Semicolon: ; */
	SEMICOLON(";"),
	/** Left shift and assign: <<= */
	LSASSIGN("<<="),
	/** Left shift: << */
	LS("<<"),
	/** Less than or equal: <= */
	LESSEQUAL("<="),
	/** Less than: < */
	LESS("<"),
	/** Equal: == */
	EQUAL("=="),
	/** Assign: = */
	ASSIGN("="),
	/** Greater than or equal: >= */
	GREATEREQUAL(">="),
	/** Right shift and assign: >>= */
	RSASSIGN(">>="),
	/** Right shift with zero fill and assign: >>>= */
	RSZEROFILLASSIGN(">>>="),
	/** Right shift with zero fill: >>> */
	RSZEROFILL(">>>"),
	/** Right shift: >> */
	RS(">>"),
	/** Greater than: > */
	GREATER(">"),
	/**
	 * Conditional: ?
	 * <p>
	 * condition ? true : false
	 */
	CONDITIONAL("?"),
	/** Modulo and assign: %= */
	MODULOASSIGN("%="),
	/** Modulo: % */
	MODULO("%"),
	/** And and assign: &= */
	ANDASSIGN("&="),
	/** Logical and: && */
	LOGICALAND("&&"),
	/** And: & */
	AND("&"),
	/** Left square bracket: [ */
	LSQUAREBRACKET("["),
	/** Right square bracket: ] */
	RSQUAREBRACKET("]"),
	/** Exclusive or and assign: ^= */
	EXCLUSIVEORASSIGN("^="),
	/** Exclusive or: ^ */
	EXCLUSIVEOR("^"),
	/** Left curly bracket: { */
	LCURLYBRACKET("{"),
	/** Right curly bracket: } */
	RCURLYBRACKET("}"),
	/** Binary complement: ~ */
	BINARYCOMPLEMENT("~"),
	/** Inclusive or and assign: |= */
	INCLUSIVEORASSIGN("|="),
	/** Logical or: || */
	LOGICALOR("||"),
	/** Inclusive or: | */
	INCLUSIVEOR("|");

	private final String string;
	private final boolean keyword;

	/**
	 * Constructor. Sets the token specific string and the given keyword.
	 * 
	 * @param string
	 * @param keyword
	 */
	TokenType(String string, boolean keyword) {
		this.string = string;
		this.keyword = keyword;
	}

	/**
	 * Constructor. Sets the token specific string. Sets the keyword to false.
	 * 
	 * @param string
	 */
	TokenType(String string) {
		this(string, false);
	}

	/**
	 * Returns the token specific string.
	 * 
	 * @return
	 */
	public String getString() {
		return string;
	}

	/**
	 * Returns true if the token is a keyword. False otherwise.
	 * 
	 * @return
	 */
	public boolean isKeyword() {
		return keyword;
	}
}
