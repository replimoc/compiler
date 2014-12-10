package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.Declaration;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class LocalVariableDeclaration extends Statement implements Declaration {

	private final Expression expression;
	private final Symbol identifier;
	private int variableNumber;

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, Expression expression, int variableNumber) {
		super(position, type);
		this.identifier = identifier;
		this.expression = expression;
		this.variableNumber = variableNumber;
	}

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, Expression expression) {
		this(position, type, identifier, expression, 0);
	}

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, int variableNumber) {
		this(position, type, identifier, null, variableNumber);
	}

	public LocalVariableDeclaration(Type type, Symbol identifier, int variableNumber) {
		this(null, type, identifier, null, variableNumber);
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

	public int getVariableNumber() {
		return variableNumber;
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}

	@Override
	public Symbol getSymbol() {
		return getIdentifier();
	}

	@Override
	public boolean isLocalVariable() {
		return true;
	}
}
