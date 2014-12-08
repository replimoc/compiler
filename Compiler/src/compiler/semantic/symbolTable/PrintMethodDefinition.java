package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.type.Type;

public class PrintMethodDefinition extends MethodDefinition {

	public PrintMethodDefinition(Symbol symbol, Type type, Definition[] parameters) {
		super(symbol, type, parameters);
	}

}
