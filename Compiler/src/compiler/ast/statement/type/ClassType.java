package compiler.ast.statement.type;

import compiler.Symbol;
import compiler.lexer.Position;

public class ClassType extends Type {

	private final Symbol identifier;

	public ClassType(Position position, Symbol identifier) {
		super(position, BasicType.CLASS);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

}
