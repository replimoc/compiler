package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;

public class FieldDeclaration extends ClassMember {

	private final Type type;

	public FieldDeclaration(Type type, Symbol identifier) {
		super(identifier);
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
