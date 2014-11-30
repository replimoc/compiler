package compiler.semantic.symbolTable;

import java.util.Arrays;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.type.Type;

public class MethodDefinition extends Definition {

	private final Definition[] parameters;
	private final boolean staticMethod;

	public MethodDefinition(Symbol symbol, Type type, Definition[] parameters, AstNode node) {
		super(symbol, type, node);
		this.parameters = parameters;
		this.staticMethod = (node instanceof StaticMethodDeclaration);
	}

	public Definition[] getParameters() {
		return parameters;
	}

	public boolean isStaticMethod() {
		return staticMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(parameters);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodDefinition other = (MethodDefinition) obj;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		return true;
	}

}
