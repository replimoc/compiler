package compiler.semantic.exceptions;

import compiler.ast.statement.MethodInvocationExpression;

public class MainNotCallableException extends SemanticAnalysisException {
	private static final long serialVersionUID = 4133706982124407813L;

	public MainNotCallableException(MethodInvocationExpression methodInvocation) {
		super(methodInvocation.getPosition(), "error: Main method must not be called!");
	}
}
