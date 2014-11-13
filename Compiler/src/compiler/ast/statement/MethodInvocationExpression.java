package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class MethodInvocationExpression extends PostfixExpression {

	private final Symbol methodIdent;
	private final Expression expression;
	private final Expression[] parameters;

	public MethodInvocationExpression(Position position, Expression expression, Symbol methodIdent, Expression[] parameters) {
		super(position, expression);
		this.methodIdent = methodIdent;
		this.parameters = parameters;
		this.expression = expression;
	}

	public Symbol getMethodIdent() {
		return methodIdent;
	}

	public Expression getMethodExpression() {
		return expression;
	}

	public Expression[] getParameters() {
		return parameters;
	}

	public boolean isLocalMethod() {
		return expression == null ? true : false;
	}

	public int numberOfArguments() {
		return parameters == null ? 0 : parameters.length;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
