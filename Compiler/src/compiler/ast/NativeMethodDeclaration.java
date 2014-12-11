package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;

public class NativeMethodDeclaration extends MethodDeclaration {
	private String assemblerName;

	public NativeMethodDeclaration(String assemblerName, Symbol identifier, Type returnType, ParameterDefinition... parameters) {
		super(identifier, returnType, parameters);
		this.assemblerName = assemblerName;
	}

	@Override
	public String getAssemblerName() {
		return assemblerName;
	}
}
