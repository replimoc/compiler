package compiler.ast;

import compiler.Symbol;

public class ClassMember {
	private final Symbol identifier;

	public ClassMember(Symbol identifier) {
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}
}
