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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayType other = (ArrayType) obj;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		return true;
	}
}
