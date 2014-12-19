package compiler.ast.declaration;

import java.util.List;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class NativeMethodFixedNameDeclaration extends NativeMethodDeclaration {

	private final String assemblerName;

	public NativeMethodFixedNameDeclaration(Position position, String assemblerName, Symbol identifier, List<ParameterDeclaration> parameters,
			Type returnType) {
		super(position, identifier, parameters, returnType);
		this.assemblerName = assemblerName;
	}

	@Override
	public String getAssemblerName() {
		return assemblerName;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((assemblerName == null) ? 0 : assemblerName.hashCode());
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
		NativeMethodFixedNameDeclaration other = (NativeMethodFixedNameDeclaration) obj;
		if (assemblerName == null) {
			if (other.assemblerName != null)
				return false;
		} else if (!assemblerName.equals(other.assemblerName))
			return false;
		return true;
	}
}
