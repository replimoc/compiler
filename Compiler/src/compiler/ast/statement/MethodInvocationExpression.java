package compiler.ast.statement;

public class MethodInvocationExpression extends Expression {

	private final Expression methodExpression;
	private final Expression[] parameters;

	public MethodInvocationExpression(Expression methodExpression, Expression[] parameters) {
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
