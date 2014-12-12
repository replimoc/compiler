package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

import firm.Mode;
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

	protected static Mode getModeReference() {
		return Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64);
	}

	protected firm.Type generateFirmType() {
		firm.Type firmType = null;
		switch (this.getBasicType()) {
		case INT:
			firmType = new PrimitiveType(Mode.getIs());
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(Mode.getBu());
			break;
		case VOID:
			return null;
		case NULL:
			firmType = new PrimitiveType(getModeReference());
			break;
		default:
			break;
		}

		return firmType;
	}
}
