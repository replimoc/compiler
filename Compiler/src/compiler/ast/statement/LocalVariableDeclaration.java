package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class LocalVariableDeclaration extends Statement {
	private final Type type;
	private final Symbol identifier;
	private final Expression expression;

	private int firmVariableNumber;

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier, Expression expression) {
		super(position);
		this.type = type;
		this.identifier = identifier;
		this.expression = expression;
	}

	public LocalVariableDeclaration(Position position, Type type, Symbol identifier) {
		this(position, type, identifier, null);
	}

	public Type getType() {
		return type;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public Expression getExpression() {
		return expression;
	}

	public int getFirmVariableNumber() {
		return firmVariableNumber;
	}

	public void setFirmVariableNumber(int firmVariableNumber) {
		this.firmVariableNumber = firmVariableNumber;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
