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

	public int getNumberOfFields() {
		return fields.size();
	}

	public int getNumberOfMethods() {
		return methods.size();
	}

	public SymbolTable createClassSymbolTable() {
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.enterScope();

		for (Entry<Symbol, Definition> curr : fields.entrySet()) {
			symbolTable.insert(curr.getKey(), curr.getValue());
		}

		return symbolTable;
	}

	public Definition[] getFieldDefinitions() {
		Definition[] fields = new Definition[this.fields.size()];
		int i = 0;
		for (Entry<Symbol, Definition> curr : this.fields.entrySet()) {
			fields[i++] = curr.getValue();
		}
		return fields;
	}

	public MethodDefinition[] getMethodDefinitions() {
		MethodDefinition[] fields = new MethodDefinition[this.fields.size()];
		int i = 0;
		for (Entry<Symbol, MethodDefinition> curr : this.methods.entrySet()) {
			fields[i++] = curr.getValue();
		}
		return fields;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassScope other = (ClassScope) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (methods == null) {
			if (other.methods != null)
				return false;
		} else if (!methods.equals(other.methods))
			return false;
		return true;
	}
}