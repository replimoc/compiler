package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;

public class ParameterDefinition {

	private final Type type;
	private final Symbol identifier;

	public ParameterDefinition(Type type, Symbol identifier) {
		this.type = type;
		this.identifier = identifier;

	}

	public Type getType() {
		return type;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
