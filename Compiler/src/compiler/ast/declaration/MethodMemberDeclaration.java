package compiler.ast.declaration;

import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public abstract class MethodMemberDeclaration extends MemberDeclaration {

	private final List<ParameterDeclaration> parameters;
	private final Type returnType;

	public MethodMemberDeclaration(Position position, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType) {
		super(position, identifier);
		this.parameters = parameters;
		this.returnType = returnType;
	}

	@Override
	public Type getType() {
		return returnType;
	}

	public List<ParameterDeclaration> getParameters() {
		return parameters;
	}

	public List<ParameterDeclaration> getValidParameters() {
		return parameters;
	}

	@Override
	protected int getSortPriority() {
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
		MethodMemberDeclaration other = (MethodMemberDeclaration) obj;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

}
