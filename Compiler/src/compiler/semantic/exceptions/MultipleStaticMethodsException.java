package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class MultipleStaticMethodsException extends SemanticAnalysisException {
	private static final long serialVersionUID = -370217446816162248L;

	public MultipleStaticMethodsException(Position pos) {
		super(pos, "error: Illegal declaration of additional static method at " + pos);
	}

}
