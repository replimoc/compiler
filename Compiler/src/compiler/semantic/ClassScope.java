package compiler.semantic;

import java.util.HashMap;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;
import compiler.semantic.symbolTable.SymbolTable;

public class ClassScope {

	private final HashMap<Symbol, Definition> fields;
	private final HashMap<Symbol, MethodDefinition> methods;

	public ClassScope(HashMap<Symbol, Definition> fields, HashMap<Symbol, MethodDefinition> methods) {
		this.fields = fields;
		this.methods = methods;
	}

	public MethodDefinition getMethodDefinition(Symbol identifier) {
		return methods.get(identifier);
	}

	public Definition getFieldDefinition(Symbol identifier) {
		return fields.get(identifier);
	}

	public SymbolTable createMethodSymbolTable(Symbol identifier) {
		SymbolTable symbolTable = createClassSymbolTable();
		symbolTable.enterScope();

		MethodDefinition method = getMethodDefinition(identifier);

		for (Definition currParameter : method.getParameters()) {
			symbolTable.insert(currParameter.getSymbol(), currParameter);
		}

		return symbolTable;
	}

	public SymbolTable createClassSymbolTable() {
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.enterScope();

		for (Entry<Symbol, Definition> curr : fields.entrySet()) {
			symbolTable.insert(curr.getKey(), curr.getValue());
		}

		return symbolTable;
	}
}
