package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.visitor.AstVisitor;
import compiler.firm.FirmUtils;
import compiler.lexer.Position;

import firm.PrimitiveType;

public class Type extends AstNode {

	private final BasicType basicType;
	private firm.Type firmType;

	public Type(BasicType basicType) {
		this(null, basicType);
	}

	public Type(Position position, BasicType basicType) {
		super(position);
		this.basicType = basicType;
	}

	public final BasicType getBasicType() {
		return basicType;
	}

	public Type getSubType() {
		return null;
	}

	public Symbol getIdentifier() {
		return null;
	}

	public firm.Type getFirmType() {
		if (firmType == null)
			this.firmType = generateFirmType();
		return firmType;
	}

	public final boolean is(BasicType basicType) {
		return this.basicType == basicType;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basicType == null) ? 0 : basicType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Type))
			return false;
		Type other = (Type) obj;
		BasicType t1 = basicType;
		BasicType t2 = other.getBasicType();
		// Swap basic types if t1 is NULL.
		if (t2 == BasicType.NULL) {
			t2 = t1;
			t1 = BasicType.NULL;
		}

		// Allow also NULL as first basic type
		if (t2 != BasicType.INT && t2 != BasicType.BOOLEAN && t1 == BasicType.NULL)
			return true;
		if (basicType != other.basicType)
			return false;
		return true;
	}

	public firm.Mode getMode() {
		switch (getBasicType()) {
		case INT:
			return FirmUtils.getModeInteger();
		case BOOLEAN:
			return FirmUtils.getModeBoolean();
		case CLASS:
		case ARRAY:
		case NULL:
			return FirmUtils.getModeReference();
		default:
			return null;
		}
	}

	protected firm.Type generateFirmType() {
		return new PrimitiveType(getMode());
	}
}
