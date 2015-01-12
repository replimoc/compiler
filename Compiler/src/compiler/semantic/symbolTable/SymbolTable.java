package compiler.semantic.symbolTable;

import java.util.LinkedList;

import compiler.Symbol;
import compiler.ast.declaration.LocalVariableDeclaration;
import compiler.ast.type.Type;

public class SymbolTable {

	private final LinkedList<Change> changeStack = new LinkedList<Change>();
	private Scope currentScope = null;
	private int localVariables;
	private int maxLocalVariables = 0;

	public SymbolTable(int variableNumberOffset) {
		this.localVariables = variableNumberOffset;
	}

	public void enterScope() {
		currentScope = new Scope(currentScope, changeStack.size());
	}

	public void leaveScope() {
		maxLocalVariables = Math.max(maxLocalVariables, localVariables);

		while (changeStack.size() > currentScope.getOldChangeStackSize()) {
			Change change = changeStack.pop();
			change.getSymbol().setDeclaration(change.getPrevScope(), change.getPreviousDeclaration());
			localVariables--;
		}
		currentScope = currentScope.getParentScope();
	}

	public int insert(Symbol symbol, Type type) {
		changeStack.push(new Change(symbol, symbol.getDeclaration(), symbol.getDeclarationScope()));
		int variableNumber = localVariables++;
		symbol.setDeclaration(currentScope, new LocalVariableDeclaration(type, symbol, variableNumber));
		return variableNumber;
	}

	public boolean isDefinedInCurrentScope(Symbol symbol) {
		return symbol.getDeclarationScope() == currentScope;
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
