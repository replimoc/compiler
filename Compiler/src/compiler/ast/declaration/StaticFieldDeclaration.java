package compiler.ast.declaration;

import compiler.Symbol;
import compiler.ast.type.Type;

public class StaticFieldDeclaration extends FieldDeclaration {

	public StaticFieldDeclaration(Type type, Symbol identifier) {
		super(type, identifier);
	}

}
