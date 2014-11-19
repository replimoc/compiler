package compiler.ast.type;

import compiler.Symbol;
import compiler.lexer.Position;

public class ArrayType extends Type {

	private final Type subType;

	public ArrayType(Position position, Type subType) {
		super(position, BasicType.ARRAY);
		this.subType = subType;
	}

	@Override
	public Type getSubType() {
		return subType;
	}

	@Override
	public Symbol getIdentifier() {
		Type tmpType = subType;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}
		return tmpType.getIdentifier();
	}

}
