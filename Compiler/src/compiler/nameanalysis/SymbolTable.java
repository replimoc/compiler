package compiler.ast.nameanalysis;

import java.util.Stack;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.nameanalysis.Scope;

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
		}
		currentScope = currentScope.getParentScope();
	}
	
	void insert(Symbol symbol, Definition definition) {
		
	}
	
	Definition lookup(Symbol symbol) {
		return null;
	}
	
	boolean isDefinedInCurrentScope(Symbol symbol) {
		return symbol.getDefinitionScope() == currentScope;
	}
}
