package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.type.Type;

public class Definition {

	private final Symbol symbol;
	private final Type type;

	public Definition(Symbol symbol, Type type) {
		this.symbol = symbol;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public Symbol getSymbol() {
		return symbol;
	}
}
