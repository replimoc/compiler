package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

import firm.PrimitiveType;

public class ArrayType extends Type {

	private final Type subType;
	private firm.Type firmArrayType;

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
		return getFinalSubtype().getIdentifier();
	}

	public Type getFinalSubtype() {
		Type tmpType = subType;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}
		return tmpType;
	}

	@Override
	protected firm.Type generateFirmType() {
		return new PrimitiveType(Type.getModeReference());
	}

	public firm.Type getFirmArrayType() {
		if (firmArrayType == null) {
			this.firmArrayType = new firm.ArrayType(getSubType().getFirmType());
		}
		return firmArrayType;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
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
