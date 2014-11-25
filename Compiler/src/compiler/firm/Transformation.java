package compiler.firm;

import compiler.ast.AstNode;

public class Transformation {

	private Transformation() {
	}

	public static void transformToFirm(AstNode ast) {
		FirmGenerationVisitor firmVisitor = new FirmGenerationVisitor();
		ast.accept(firmVisitor);
	}

}
