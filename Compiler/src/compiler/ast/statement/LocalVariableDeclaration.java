package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;
import compiler.semantic.symbolTable.LocalVariableDefinition;

public class LocalVariableDeclaration extends Statement {

	private final Expression expression;
	private final Symbol identifier;
	private int variableNumber;

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, Expression expression) {
		super(position, type);
		this.identifier = identifier;
		this.expression = expression;
	}

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier) {
		this(position, type, identifier, null);
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public LocalVariableDefinition getDefinition() {
		return new LocalVariableDefinition(identifier, type, variableNumber);
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}
}
