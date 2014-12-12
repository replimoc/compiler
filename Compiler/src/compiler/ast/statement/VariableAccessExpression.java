package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.declaration.Declaration;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class VariableAccessExpression extends PostfixExpression {
	private final Expression expression;
	private final Symbol fieldIdentifier;
	private Declaration definition;

	public VariableAccessExpression(Position position, Expression leftExpression, Symbol fieldIdentifier) {
		super(position, leftExpression);
		this.expression = leftExpression;
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

	public Declaration getDefinition() {
		return definition;
	}

	public void setDefinition(Declaration newDef) {
		definition = newDef;
	}
}
