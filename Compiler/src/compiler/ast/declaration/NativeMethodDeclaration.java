package compiler.ast.declaration;

import compiler.Symbol;
import compiler.ast.type.Type;

public class NativeMethodDeclaration extends MethodDeclaration {
	private String assemblerName;

	public NativeMethodDeclaration(String assemblerName, Symbol identifier, Type returnType, ParameterDeclaration... parameters) {
		super(identifier, returnType, parameters);
		this.assemblerName = assemblerName;
	}

	@Override
	public String getAssemblerName() {
		return assemblerName;
	}
}
