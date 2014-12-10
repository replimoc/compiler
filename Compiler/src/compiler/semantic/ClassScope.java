package compiler.semantic;

import java.util.HashMap;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.Declaration;
import compiler.ast.MethodDeclaration;

public class ClassScope {

	private final HashMap<Symbol, Declaration> fields;
	private final HashMap<Symbol, MethodDeclaration> methods;

	public ClassScope(HashMap<Symbol, Declaration> fields, HashMap<Symbol, MethodDeclaration> methods) {
		this.fields = fields;
		this.methods = methods;
	}

	public MethodDeclaration getMethodDefinition(Symbol identifier) {
		return methods.get(identifier);
	}

	public Declaration getFieldDefinition(Symbol identifier) {
		return fields.get(identifier);
	}

	public int getNumberOfFields() {
		return fields.size();
	}

	public int getNumberOfMethods() {
		return methods.size();
	}

	public Declaration[] getFieldDefinitions() {
		Declaration[] fields = new Declaration[this.fields.size()];
		int i = 0;
		for (Entry<Symbol, Declaration> curr : this.fields.entrySet()) {
			fields[i++] = curr.getValue();
		}
		return fields;
	}

	public MethodDeclaration[] getMethodDefinitions() {
		MethodDeclaration[] methods = new MethodDeclaration[this.methods.size()];
		int i = 0;
		for (Entry<Symbol, MethodDeclaration> curr : this.methods.entrySet()) {
			methods[i++] = curr.getValue();
		}
		return methods;
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
