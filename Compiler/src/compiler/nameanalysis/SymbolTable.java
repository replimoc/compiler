package compiler.nameanalysis;

import java.util.Stack;

import compiler.StringTable;
import compiler.Symbol;
import compiler.nameanalysis.Scope;

public class SymbolTable {

	private final StringTable stringTable;
	private final Stack<Change> changeStack = new Stack<Change>();
	private Scope currentScope;
	
	public SymbolTable(StringTable stringTable) {
		this.stringTable = stringTable;
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
