package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class VariableAccessExpression extends Expression {
	private final Expression expr;
	private final Symbol fieldIdentifier;

	public VariableAccessExpression(Position position, Expression expr, Symbol fieldIdentifier) {
		super(position);
		this.expr = expr;
		this.fieldIdentifier = fieldIdentifier;
	}

	public Expression getExpression() {
		return expr;
	}

	public Symbol getFieldIdentifier() {
		return fieldIdentifier;
	}

	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
