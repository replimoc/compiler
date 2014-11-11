package compiler.parser.printer;

import compiler.ast.statement.Expression;

public class PrettyPrinter {

	public static String get(Expression expression) {
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
		expression.accept(visitor);
		return visitor.getOutputString();
	}
}
