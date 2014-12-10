package compiler.semantic.symbolTable;

import java.util.LinkedList;

import compiler.Symbol;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.type.Type;

public class SymbolTable {

	private final LinkedList<Change> changeStack = new LinkedList<Change>();
	private Scope currentScope = null;
	private int localVariables = 0;
	private int maxLocalVariables = 0;

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

	public int insert(Symbol symbol, Type type) {
		changeStack.push(new Change(symbol, symbol.getDefinition(), symbol.getDefinitionScope()));
		int variableNumber = localVariables + 1; // +1 for this pointer
		symbol.setDefintion(currentScope, new LocalVariableDeclaration(type, symbol, variableNumber));
		localVariables++;
		return variableNumber;
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
