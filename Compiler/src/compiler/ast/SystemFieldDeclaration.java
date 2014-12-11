package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;

public class SystemFieldDeclaration extends FieldDeclaration {

	public SystemFieldDeclaration(Type type, Symbol identifier) {
		super(type, identifier);
	}

}
