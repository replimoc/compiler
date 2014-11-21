package compiler.ast.type;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class Type extends AstNode {

	private final BasicType basicType;

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

}
