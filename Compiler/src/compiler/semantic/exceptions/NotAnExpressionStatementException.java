package compiler.semantic.exceptions;

import compiler.lexer.Position;

public class NotAnExpressionStatementException extends SemanticAnalysisException {

	private static final long serialVersionUID = 7607378965250581103L;

	public NotAnExpressionStatementException(Position position) {
		super(position, "error: Insert assignment operator to complete expression at " + position);
	}
}
