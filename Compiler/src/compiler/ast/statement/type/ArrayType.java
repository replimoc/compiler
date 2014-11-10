package compiler.ast.statement.type;

import compiler.lexer.Position;

public class ArrayType extends Type {

	private final Type subType;

	public ArrayType(Position position, Type subType) {
		super(position, BasicType.ARRAY);
		this.subType = subType;
	}

	public Type getSubType() {
		return subType;
	}

}
