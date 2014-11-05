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
	ERROR("error", OperationType.UNDEFINED),
	EOF("EOF", OperationType.UNDEFINED),
	IDENTIFIER("identifier", OperationType.UNDEFINED),
	INTEGER("integer", OperationType.UNDEFINED),

	/* key words */
	ABSTRACT("abstract", OperationType.KEYWORD),
	ASSERT("assert", OperationType.KEYWORD),
	BOOLEAN("boolean", OperationType.KEYWORD),
	BREAK("break", OperationType.KEYWORD),
	BYTE("byte", OperationType.KEYWORD),
	CASE("case", OperationType.KEYWORD),
	CATCH("catch", OperationType.KEYWORD),
	CHAR("char", OperationType.KEYWORD),
	CLASS("class", OperationType.KEYWORD),
	CONST("const", OperationType.KEYWORD),
	CONTINUE("continue", OperationType.KEYWORD),
	DEFAULT("default", OperationType.KEYWORD),
	DOUBLE("double", OperationType.KEYWORD),
	DO("do", OperationType.KEYWORD),
	ELSE("else", OperationType.KEYWORD),
	ENUM("enum", OperationType.KEYWORD),
	EXTENDS("extends", OperationType.KEYWORD),
	FALSE("false", OperationType.KEYWORD),
	FINALLY("finally", OperationType.KEYWORD),
	FINAL("final", OperationType.KEYWORD),
	FLOAT("float", OperationType.KEYWORD),
	FOR("for", OperationType.KEYWORD),
	GOTO("goto", OperationType.KEYWORD),
	IF("if", OperationType.KEYWORD),
	IMPLEMENTS("implements", OperationType.KEYWORD),
	IMPORT("import", OperationType.KEYWORD),
	INSTANCEOF("instanceof", OperationType.KEYWORD),
	INTERFACE("interface", OperationType.KEYWORD),
	INT("int", OperationType.KEYWORD),
	LONG("long", OperationType.KEYWORD),
	NATIVE("native", OperationType.KEYWORD),
	NEW("new", OperationType.KEYWORD),
	NULL("null", OperationType.KEYWORD),
	PACKAGE("package", OperationType.KEYWORD),
	PRIVATE("private", OperationType.KEYWORD),
	PROTECTED("protected", OperationType.KEYWORD),
	PUBLIC("public", OperationType.KEYWORD),
	RETURN("return", OperationType.KEYWORD),
	SHORT("short", OperationType.KEYWORD),
	STATIC("static", OperationType.KEYWORD),
	STRICTFP("strictfp", OperationType.KEYWORD),
	SUPER("super", OperationType.KEYWORD),
	SWITCH("switch", OperationType.KEYWORD),
	SYNCHRONIZED("synchronized", OperationType.KEYWORD),
	THIS("this", OperationType.KEYWORD),
	THROWS("throws", OperationType.KEYWORD),
	THROW("throw", OperationType.KEYWORD),
	TRANSIENT("transient", OperationType.KEYWORD),
	TRUE("true", OperationType.KEYWORD),
	TRY("try", OperationType.KEYWORD),
	VOID("void", OperationType.KEYWORD),
	VOLATILE("volatile", OperationType.KEYWORD),
	WHILE("while", OperationType.KEYWORD),

	/* operators */
	/** Not equal: != */
	NOTEQUAL("!=", OperationType.BINARY, 3, true),
	/** Logical not: ! */
	LOGICALNOT("!"),
	/** Left parenthesis: ( */
	LP("("),
	/** Right parenthesis: ) */
	RP(")"),
	/** Multiply and assign: *= */
	MULTIPLYASSIGN("*="),
	/** Multiply: * */
	MULTIPLY("*", OperationType.BINARY, 1, true),
	/** Increment: ++ */
	INCREMENT("++"),
	/** Add and assign: += */
	ADDASSIGN("+="),
	/** Add: + */
	ADD("+", OperationType.BINARY, 2, true),
	/** Comma: , */
	COMMA(","),
	/** Subtract and assign: -= */
	SUBTRACTASSIGN("-="),
	/** Decrement: -- */
	DECREMENT("--"),
	/** Subtract: - */
	SUBTRACT("-", OperationType.BINARY, 2, true),
	/** Point: . */
	POINT("."),
	/** Divide and assign: /= */
	DIVIDEASSIGN("/="),
	/** Divide: / */
	DIVIDE("/", OperationType.BINARY, 1, true),
	/** Colon: : */
	COLON(":"),
	/** Semicolon: ; */
	SEMICOLON(";"),
	/** Left shift and assign: <<= */
	LSASSIGN("<<="),
	/** Left shift: << */
	LS("<<"),
	/** Less than or equal: <= */
	LESSEQUAL("<=", OperationType.BINARY, 3, true),
	/** Less than: < */
	LESS("<", OperationType.BINARY, 3, true),
	/** Equal: == */
	EQUAL("==", OperationType.BINARY, 4, true),
	/** Assign: = */
	ASSIGN("=", OperationType.BINARY, 5, false),
	/** Greater than or equal: >= */
	GREATEREQUAL(">=", OperationType.BINARY, 3, true),
	/** Right shift and assign: >>= */
	RSASSIGN(">>="),
	/** Right shift with zero fill and assign: >>>= */
	RSZEROFILLASSIGN(">>>="),
	/** Right shift with zero fill: >>> */
	RSZEROFILL(">>>"),
	/** Right shift: >> */
	RS(">>"),
	/** Greater than: > */
	GREATER(">", OperationType.BINARY, 3, true),
	/**
	 * Conditional: ?
	 * <p>
	 * condition ? true : false
	 */
	CONDITIONAL("?"),
	/** Modulo and assign: %= */
	MODULOASSIGN("%="),
	/** Modulo: % */
	MODULO("%", OperationType.BINARY, 1, true),
	/** And and assign: &= */
	ANDASSIGN("&="),
	/** Logical and: && */
	LOGICALAND("&&", OperationType.BINARY, 4, true),
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
	LOGICALOR("||", OperationType.BINARY, 3, true),
	/** Inclusive or: | */
	INCLUSIVEOR("|");

	private final String string;
	private final OperationType operationType;
	private final int precedence;
	private final boolean leftAssociative;

	/**
	 * Constructor. Sets the token specific string and the given keyword, it also specifies the precidence of an operator.
	 * 
	 * @param string
	 * @param operationType
	 * @param precendence
	 */
	TokenType(String string, OperationType operationType, int precedence, boolean leftAssociative) {
		this.string = string;
		this.operationType = operationType;
		this.precedence = precedence;
		this.leftAssociative = leftAssociative;
	}

	/**
	 * Constructor. Sets the token specific string and the given keyword.
	 * 
	 * @param string
	 * @param operationType
	 */
	TokenType(String string, OperationType operationType) {
		this(string, operationType, -1, true);
	}

	/**
	 * Constructor. Sets the token specific string. Sets the keyword to false.
	 * 
	 * @param string
	 */
	TokenType(String string) {
		this(string, OperationType.UNDEFINED);
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
	 * Return the type of an token.
	 * 
	 * @return
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * Returns true if the token is a keyword. False otherwise.
	 * 
	 * @return
	 */
	public boolean isKeyword() {
		return operationType == OperationType.KEYWORD;
	}

	/**
	 * Returns the precedence of an token.
	 * 
	 * @return
	 */
	public int getPrecedence() {
		return precedence;
	}

	public boolean isLeftAssociative() {
		return leftAssociative;
	}
}
