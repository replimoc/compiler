package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.type.Type;

public class Definition {

	private final Symbol symbol;
	private final Type type;
	private final AstNode node;

	public Definition(Symbol symbol, Type type, AstNode node) {
		this.symbol = symbol;
		this.type = type;
		this.node = node;
	}

	public Type getType() {
		return type;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public AstNode getAstNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Definition other = (Definition) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
