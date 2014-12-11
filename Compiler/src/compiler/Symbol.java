package compiler;

import compiler.ast.Declaration;
import compiler.semantic.symbolTable.Scope;

/**
 * Symbol represents a hashed String (see StringTable).
 */
public class Symbol {
	private final String value;
	private Scope defScope;
	private Declaration definition;

	// definition

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
	 * Sets a reference to a definition in a certain scope.
	 * 
	 * @param scope
	 *            Scope of definition
	 * @param definition
	 *            Definition object
	 */
	public void setDefintion(Scope scope, Declaration definition) {
		this.defScope = scope;
		this.definition = definition;
	}

	/**
	 * Returns the current definition scope. May be null.
	 * 
	 * @return Current definition scope.
	 */
	public Scope getDefinitionScope() {
		return defScope;
	}

	/**
	 * Returns the current definition. May be null.
	 * 
	 * @return Current definition
	 */
	public Declaration getDefinition() {
		return definition;
	}

	public boolean isDefined() {
		return defScope != null && definition != null;
	}
}
