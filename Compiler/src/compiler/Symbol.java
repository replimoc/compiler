package compiler;

import compiler.nameanalysis.Definition;
import compiler.nameanalysis.Scope;

/**
 * Symbol represents a hashed String (see StringTable).
 */
public class Symbol {
	private String value;
	private Scope defScope;
	private Definition definition;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Symbol other = (Symbol) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public void setDefintion(Scope scope, Definition definition) {
		this.defScope = scope;
		this.definition = definition;
	}

	public Scope getDefinitionScope() {
		return defScope;
	}
	
	public Definition getDefinition() {
		return definition;
	}

}
