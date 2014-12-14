package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.declaration.Declaration;

class Change {

	private final Symbol symbol;
	private final Declaration previousDeclaration;
	private final Scope prevScope;

	protected Change(Symbol symbol, Declaration declaration, Scope prevScope) {
		this.symbol = symbol;
		this.previousDeclaration = declaration;
		this.prevScope = prevScope;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public Declaration getPreviousDeclaration() {
		return previousDeclaration;
	}

	public Scope getPrevScope() {
		return prevScope;
	}
}
