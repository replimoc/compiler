package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
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
}
