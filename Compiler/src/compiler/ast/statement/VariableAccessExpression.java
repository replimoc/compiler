package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class VariableAccessExpression extends PostfixExpression {
	private final Expression expression;
	private final Symbol fieldIdentifier;

	public VariableAccessExpression(Position position, Expression expression, Symbol fieldIdentifier) {
		super(position, expression);
		this.expression = expression;
		this.fieldIdentifier = fieldIdentifier;
	}

	public Expression getExpression() {
		return expression;
	}

	public Symbol getFieldIdentifier() {
		return fieldIdentifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
