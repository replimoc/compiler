package compiler.ast.statement;

import compiler.Symbol;
import compiler.ast.statement.type.Type;

public class LocalVariableDeclaration extends Statement {
	private final Type type;
	private final Symbol identifier;
	private final Expression expression;

	public LocalVariableDeclaration(Type type, Symbol identifier, Expression expression) {
		this.type = type;
		this.identifier = identifier;
		this.expression = expression;
	}

	public LocalVariableDeclaration(Type type, Symbol identifier) {
		this(type, identifier, null);
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
}
