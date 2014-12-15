package compiler.semantic;

import java.util.HashMap;
import java.util.Map.Entry;

import compiler.Symbol;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.Declaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MethodMemberDeclaration;
import compiler.ast.declaration.StaticFieldDeclaration;

public class ClassScope {
	private final ClassDeclaration classDeclaration;
	private final HashMap<Symbol, FieldDeclaration> fields;
	private final HashMap<Symbol, MethodMemberDeclaration> methods;

	public ClassScope(HashMap<Symbol, FieldDeclaration> fields, HashMap<Symbol, MethodMemberDeclaration> methods) {
		this(null, fields, methods);
	}

	public ClassScope(ClassDeclaration classDeclaration, HashMap<Symbol, FieldDeclaration> fields, HashMap<Symbol, MethodMemberDeclaration> methods) {
		this.classDeclaration = classDeclaration;
		this.fields = fields;
		this.methods = methods;
	}

	public ClassDeclaration getClassDeclaration() {
		return classDeclaration;
	}

	public MethodMemberDeclaration getMethodDeclaration(Symbol identifier) {
		return methods.get(identifier);
	}

	public Declaration getFieldDeclaration(Symbol identifier) {
		return fields.get(identifier);
	}

	public int getNumberOfFields() {
		return fields.size();
	}

	public int getNumberOfMethods() {
		return methods.size();
	}

	public boolean hasStaticField() {
		for (Entry<Symbol, FieldDeclaration> curr : this.fields.entrySet()) {
			if (curr.getValue() instanceof StaticFieldDeclaration) {
				return true;
			}
		}
		return false;
	}

	public FieldDeclaration[] getFieldDeclarations() {
		FieldDeclaration[] fields = new FieldDeclaration[this.fields.size()];
		int i = 0;
		for (Entry<Symbol, FieldDeclaration> curr : this.fields.entrySet()) {
			fields[i++] = curr.getValue();
		}
		return fields;
	}

	public MethodMemberDeclaration[] getMethodDeclarations() {
		MethodMemberDeclaration[] methods = new MethodMemberDeclaration[this.methods.size()];
		int i = 0;
		for (Entry<Symbol, MethodMemberDeclaration> curr : this.methods.entrySet()) {
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
