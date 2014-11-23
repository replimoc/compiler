package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class NoMainFoundException extends SemanticAnalysisException {
	private static final long serialVersionUID = 709713290711218056L;

	public NoMainFoundException() {
		super(new Position(0, 0), "error: No 'public static void main(String[] args)' method has been found.");
	}
}
