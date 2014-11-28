package compiler.semantic.symbolTable;

import java.util.LinkedList;

import compiler.Symbol;

public class SymbolTable {

	private final LinkedList<Change> changeStack = new LinkedList<Change>();
	private Scope currentScope;
	private int localVariables;
	private int maxLocalVariables;

	public SymbolTable() {
		currentScope = null;
	}

	public void enterScope() {
		currentScope = new Scope(currentScope, changeStack.size());
	}

	public void leaveScope() {
		maxLocalVariables = Math.max(maxLocalVariables, localVariables);

		while (changeStack.size() > currentScope.getOldChangeStackSize()) {
			Change change = changeStack.pop();
			change.getSymbol().setDefintion(change.getPrevScope(), change.getPrevDefinition());
			localVariables--;
		}
		currentScope = currentScope.getParentScope();
	}

	public void insert(Symbol symbol, Definition definition) {
		changeStack.push(new Change(symbol, symbol.getDefinition(), symbol.getDefinitionScope()));
		symbol.setDefintion(currentScope, definition);
		localVariables++;
	}

	public boolean isDefinedInCurrentScope(Symbol symbol) {
		return symbol.getDefinitionScope() == currentScope;
	}

	public void leaveAllScopes() {
		while (currentScope != null) {
			leaveScope();
		}
	}

	public int getRequiredLocalVariables() {
		return maxLocalVariables;
	}
}
