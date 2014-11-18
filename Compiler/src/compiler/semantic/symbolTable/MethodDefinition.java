package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.type.Type;

public class MethodDefinition extends Definition {

	private final Definition[] parameters;

	public MethodDefinition(Symbol symbol, Type type, Definition[] parameters) {
		super(symbol, type);
		this.parameters = parameters;
	}

	public Definition[] getParameters() {
		return parameters;
	}
}
