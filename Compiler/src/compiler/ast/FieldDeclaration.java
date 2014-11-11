package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.type.Type;
import compiler.lexer.Position;

public class FieldDeclaration extends ClassMember {

	public FieldDeclaration(Position position, Type type, Symbol identifier) {
		super(position, identifier, type);
	}
}
