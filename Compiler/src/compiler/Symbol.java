package compiler;

import compiler.ast.declaration.Declaration;
import compiler.semantic.symbolTable.Scope;

/**
 * Symbol represents a hashed String (see StringTable).
 */
public class Symbol implements Comparable<Symbol> {
	private final String value;
	private Scope scope;
	private Declaration declaration;

	public Symbol(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	/**
	 * Sets a reference to a {@link Declaration} in a certain scope.
	 * 
	 * @param scope
	 *            Scope of declaration
	 * @param declaration
	 *            {@link Declaration} object
	 */
	public void setDeclaration(Scope scope, Declaration declaration) {
		this.scope = scope;
		this.declaration = declaration;
	}

	/**
	 * Returns the current {@link Declaration}'s scope. May be null.
	 * 
	 * @return Current {@link Declaration}'s scope.
	 */
	public Scope getDeclarationScope() {
		return scope;
	}

	/**
	 * Returns the current {@link Declaration}. May be null.
	 * 
	 * @return Current {@link Declaration}
	 */
	public Declaration getDeclaration() {
		return declaration;
	}

	public boolean isDefined() {
		return scope != null && declaration != null;
	}

	@Override
	public int compareTo(Symbol other) {
		return value.compareTo(other.value);
	}
}
