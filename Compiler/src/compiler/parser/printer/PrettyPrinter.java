package compiler.parser.printer;

import compiler.ast.AstNode;

public class PrettyPrinter {

	public static String get(AstNode expression) {
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
		expression.accept(visitor);
		return visitor.getOutputString();
	}
}
