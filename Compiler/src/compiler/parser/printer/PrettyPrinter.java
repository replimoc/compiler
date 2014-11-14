package compiler.parser.printer;

import compiler.ast.AstNode;

public class PrettyPrinter {

	public static String get(AstNode astNode) {
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
		astNode.accept(visitor);
		return visitor.getOutputString();
	}
}
