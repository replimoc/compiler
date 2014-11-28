package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.symbolTable.Definition;

public class VariableAccessExpression extends PostfixExpression {
	private final Expression expression;
	private final Symbol fieldIdentifier;
	private Definition definition;

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

	public Definition getDefinition() {
		return definition;
	}

	public void setDefinition(Definition newDef) {
		definition = newDef;
	}
}
