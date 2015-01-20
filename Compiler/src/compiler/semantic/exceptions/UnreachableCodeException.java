package compiler.semantic.exceptions;

import compiler.ast.statement.Statement;

public class UnreachableCodeException extends SemanticAnalysisException {
	private static final long serialVersionUID = -2799408169689623306L;

	public UnreachableCodeException(Statement statement) {
		super(statement.getPosition(), "error: Unreachable code in line " + statement.getPosition());
	}
}
