package compiler.semantic.symbolTable;

import java.util.LinkedList;

import compiler.Symbol;

public class SymbolTable {

	private final LinkedList<Change> changeStack = new LinkedList<Change>();
	private Scope currentScope;

	public SymbolTable() {
		currentScope = null;
	}

	void enterScope() {
		currentScope = new Scope(currentScope, changeStack.size());
	}

	void leaveScope() {
		while (changeStack.size() > currentScope.getOldChangeStackSize()) {
			Change change = changeStack.pop();
			change.getSymbol().setDefintion(change.getPrevScope(), change.getPrevDefinition());
		}
		currentScope = currentScope.getParentScope();
	}

	void insert(Symbol symbol, Definition definition) {
		changeStack.push(new Change(symbol, symbol.getDefinition(), symbol.getDefinitionScope()));
		symbol.setDefintion(currentScope, definition);
	}

	boolean isDefinedInCurrentScope(Symbol symbol) {
		return symbol.getDefinitionScope() == currentScope;
	}
}
