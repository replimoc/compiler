package compiler.parser.printer;

import compiler.ast.AstNode;

public final class PrettyPrinter {

	private PrettyPrinter() { // no objects of this class allowed
	}

	public static String prettyPrint(AstNode astNode) {
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
		astNode.accept(visitor);
		return visitor.getOutputString();
	}
}
