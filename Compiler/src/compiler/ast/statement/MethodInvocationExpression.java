package compiler.ast.statement;

import compiler.Symbol;
import compiler.lexer.Position;

public class MethodInvocationExpression extends Expression {

	private final Symbol methodIdent;
	private final Expression expr;
	private final Expression[] parameters;

	public MethodInvocationExpression(Position position, Expression expr, Symbol methodIdent, Expression[] parameters) {
		super(position);
		this.methodIdent = methodIdent;
		this.parameters = parameters;
		this.expr = expr;
	}

	public Symbol getMethodIdent() {
		return methodIdent;
	}

	public Expression getMethodExpression() {
		return expr;
	}

	public Expression[] getParameters() {
		return parameters;
	}

}
