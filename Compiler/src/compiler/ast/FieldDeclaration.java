package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class FieldDeclaration extends ClassMember {
	private static final String MEMBER_TYPE = "f";
	private final Type type;

	public FieldDeclaration(Type type, Symbol identifier) {
		this(null, type, identifier);
	}

	public FieldDeclaration(Position position, Type type, Symbol identifier) {
		super(position, identifier);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected int getSortPriority() {
		return 1;
	}

	@Override
	public boolean isLocalVariable() {
		return false;
	}

	@Override
	public String getMemberType() {
		return MEMBER_TYPE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		FieldDeclaration other = (FieldDeclaration) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
