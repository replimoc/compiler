package compiler.ast.statement;

import compiler.lexer.Position;

public class MethodInvocationExpression extends Expression {

	private final Expression methodExpression;
	private final Expression[] parameters;

	public MethodInvocationExpression(Position position, Expression methodExpression, Expression[] parameters) {
		super(position);
		this.methodExpression = methodExpression;
		this.parameters = parameters;
	}

	public Expression getMethodExpression() {
		return methodExpression;
	}

	public Expression[] getParameters() {
		return parameters;
	}

}
