package compiler.ast;

import compiler.Symbol;
import compiler.ast.type.Type;

public interface Declaration {
	public Type getType();

	public Symbol getSymbol();

	public boolean isLocalVariable();
}
