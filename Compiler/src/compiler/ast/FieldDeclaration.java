package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class FieldDeclaration extends ClassMember {
	private final Type type;

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
}
