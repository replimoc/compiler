package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.type.Type;

public class LocalVariableDefinition extends Definition {

	private final int variableNumber;

	public LocalVariableDefinition(Symbol symbol, Type type, int variableNumber) {
		super(symbol, type);
		this.variableNumber = variableNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	@Override
	public boolean isLocalVariable() {
		return true;
	}

}
