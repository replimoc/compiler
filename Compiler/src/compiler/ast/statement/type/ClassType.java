package compiler.ast.statement.type;

import compiler.Symbol;

public class ClassType extends Type {

	private final Symbol identifier;

	public ClassType(Symbol identifier) {
		super(BasicType.CLASS);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

}
