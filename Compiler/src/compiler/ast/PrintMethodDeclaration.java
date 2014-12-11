package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public class PrintMethodDeclaration extends MethodDeclaration {

	public PrintMethodDeclaration(Position position, Symbol identifier, Type returnType) {
		super(position, identifier, returnType);
	}

	@Override
	public String getAssemblerName() {
		return "print_int";
	}
}
