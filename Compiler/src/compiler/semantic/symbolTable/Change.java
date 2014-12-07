package compiler.semantic.symbolTable;

import compiler.Symbol;

class Change {

	private final Symbol symbol;
	private final Definition prevDefinition;
	private final Scope prevScope;

	protected Change(Symbol symbol, Definition prevDefinition, Scope prevScope) {
		this.symbol = symbol;
		this.prevDefinition = prevDefinition;
		this.prevScope = prevScope;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Definition getPrevDefinition() {
		return prevDefinition;
	}

	public Scope getPrevScope() {
		return prevScope;
	}
}
