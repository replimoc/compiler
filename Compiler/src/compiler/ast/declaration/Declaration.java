package compiler.ast.declaration;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public abstract class Declaration extends AstNode {
	protected final Symbol identifier;
	private ClassDeclaration classDeclaration;

	public Declaration(Position position, Symbol identifier) {
		super(position);
		this.identifier = identifier;
	}

	public Declaration(Position position, Type type, Symbol identifier) {
		super(position, type);
		this.identifier = identifier;
	}

	public Symbol getIdentifier() {
		return identifier;
	}

	public String getAssemblerName() {
		return "_" + escapeName(getClassName()) + "_"
				+ escapeName(getMemberType()) + "_"
				+ escapeName(getIdentifier().getValue());
	}

	private String escapeName(String name) {
		return name.replaceAll("_", "__");
	}

	public abstract String getMemberType();

	public void setClassDeclaration(ClassDeclaration classDeclaration) {
		this.classDeclaration = classDeclaration;
	}

	public ClassDeclaration getClassDeclaration() {
		return classDeclaration;
	}

	public String getClassName() {
		String className = "";
		if (getClassDeclaration() != null) { // TODO: Currently there is no class declaration for PrintStream
			className = getClassDeclaration().getIdentifier().getValue();
		}
		return className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Declaration other = (Declaration) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}
