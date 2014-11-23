package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class MissingReturnStatementOnAPathException extends SemanticAnalysisException {

	private static final long serialVersionUID = -7479500017868417422L;

	public MissingReturnStatementOnAPathException(Position position) {
		super(position, "At least one path does not have a return statement.");
	}
}
