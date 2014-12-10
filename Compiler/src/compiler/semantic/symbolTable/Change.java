package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.Declaration;

class Change {

	private final Symbol symbol;
	private final Declaration prevDefinition;
	private final Scope prevScope;

	protected Change(Symbol symbol, Declaration declaration, Scope prevScope) {
		this.symbol = symbol;
		this.prevDefinition = declaration;
		this.prevScope = prevScope;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Declaration getPrevDefinition() {
		return prevDefinition;
	}

	public Scope getPrevScope() {
		return prevScope;
	}
}
