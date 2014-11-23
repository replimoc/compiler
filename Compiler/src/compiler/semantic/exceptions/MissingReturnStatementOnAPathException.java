package compiler.semantic.exceptions;

import compiler.Symbol;
import compiler.lexer.Position;

public class MissingReturnStatementOnAPathException extends SemanticAnalysisException {

	private static final long serialVersionUID = -7479500017868417422L;

	public MissingReturnStatementOnAPathException(Position position, Symbol methodIdentifier) {
		super(position, "error: At least one path in method '" + methodIdentifier.getValue() + "' at " + position
				+ " does not have a return statement.");
	}
}
