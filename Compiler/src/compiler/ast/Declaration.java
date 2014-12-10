package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public abstract class Declaration extends AstNode {
	private final Symbol symbol;

	public Declaration(Position position, Symbol symbol) {
		super(position);
		this.symbol = symbol;
	}

	public Declaration(Position position, Type type, Symbol symbol) {
		super(position, type);
		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public boolean isLocalVariable() {
		return false;
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
