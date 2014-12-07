package compiler.semantic.symbolTable;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.type.Type;

public class PrintMethodDefinition extends MethodDefinition {

	public PrintMethodDefinition(Symbol symbol, Type type, Definition[] parameters, AstNode node) {
		super(symbol, type, parameters, node);
	}

}
