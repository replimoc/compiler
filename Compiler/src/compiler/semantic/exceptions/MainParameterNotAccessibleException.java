package compiler.semantic.exceptions;

import compiler.ast.statement.VariableAccessExpression;

public class MainParameterNotAccessibleException extends SemanticAnalysisException {
	private static final long serialVersionUID = 9074801993003507209L;

	public MainParameterNotAccessibleException(VariableAccessExpression expression) {
		super(expression.getPosition(), "error: Access to parameter String[] " + expression.getFieldIdentifier() + " of main method at "
				+ expression.getPosition());
	}

}
