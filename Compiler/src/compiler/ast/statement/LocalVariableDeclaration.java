package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.symbolTable.Definition;

public class LocalVariableDeclaration extends Statement {

	private final Definition definition;
	private final Expression expression;

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, Expression expression) {
		super(position);
		this.definition = new Definition(identifier, type, this);
		this.expression = expression;
	}

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier) {
		this(position, type, identifier, null);
	}

	@Override
	public Type getType() {
		return definition.getType();
	}

	public Symbol getIdentifier() {
		return definition.getSymbol();
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public Definition getDefinition() {
		return definition;
	}

}
