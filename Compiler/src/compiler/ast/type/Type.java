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

	public BasicType getBasicType() {
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

	public boolean is(BasicType basicType) {
		return getBasicType() == basicType;
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
		if (getClass() != obj.getClass())
			return false;
		Type other = (Type) obj;
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
		firm.Mode mode = getMode();
		if (mode != null) {
			return new PrimitiveType(mode);
		}
		return null;
	}
}
