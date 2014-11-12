package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class VariableAccessExpression extends Expression {
	private final Expression expressions;
	private final Symbol fieldIdentifier;

	public VariableAccessExpression(Position position, Expression expressions, Symbol fieldIdentifier) {
		super(position);
		this.expressions = expressions;
		this.fieldIdentifier = fieldIdentifier;
	}

	public Expression getExpression() {
		return expressions;
	}

	public Symbol getFieldIdentifier() {
		return fieldIdentifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
