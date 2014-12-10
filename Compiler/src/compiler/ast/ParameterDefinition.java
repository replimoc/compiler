package compiler.ast;

import compiler.Symbol;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.Position;

public class ParameterDefinition extends Declaration {

	private final Symbol identifier;
	private int variableNumber;

	public ParameterDefinition(Position position, Type type, Symbol identifier) {
		super(position, type, identifier);
		this.identifier = identifier;

	}

	public ParameterDefinition(Type type, Symbol identifier) {
		this(null, type, identifier);

	}

	public Symbol getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

	public Declaration getDefinition() { // TODO maybe optimize this
		return new LocalVariableDeclaration(getPosition(), getType(), getIdentifier(), variableNumber);
	}

	public void setVariableNumber(int variableNumber) {
		this.variableNumber = variableNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	@Override
	public Symbol getSymbol() {
		return getIdentifier();
	}

	@Override
	public boolean isLocalVariable() {
		return false;
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
		ParameterDefinition other = (ParameterDefinition) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}
