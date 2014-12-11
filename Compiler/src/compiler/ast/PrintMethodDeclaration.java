package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;

public class PrintMethodDeclaration extends MethodDeclaration {

	public PrintMethodDeclaration(Symbol identifier, Type returnType, ParameterDefinition... parameters) {
		super(identifier, returnType, parameters);
	}

	@Override
	public String getAssemblerName() {
		return "print_int";
	}
}
