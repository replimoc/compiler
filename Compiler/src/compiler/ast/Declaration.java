package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public abstract class Declaration extends AstNode {
	private final Symbol symbol;
	private ClassDeclaration classDeclaration;

	public Declaration(Position position, Symbol symbol) {
		super(position);
		this.symbol = symbol;
	}

	public Declaration(Position position, Type type, Symbol symbol) {
		super(position, type);
		this.symbol = symbol;
	}

	public Symbol getIdentifier() {
		return symbol;
	}

	public String getAssemblerName() {
		String className = "";
		if (getClassDeclaration() != null) { // TODO: Currently there is no class declaration for PrintStream
			className = getClassDeclaration().getIdentifier().getValue();
		}
		return "_" + escapeName(className) + "_"
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
}
