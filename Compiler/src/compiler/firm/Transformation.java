package compiler.firm;

import compiler.ast.AstNode;

public class Transformation {

	private Transformation() {
	}

	public static void transformToFirm(AstNode ast) {
		FirmHierarchy hierarchy = new FirmHierarchy();
		// build type hierarchy
		FirmHierarchyGenerationVisitor hierarchyGenerationVisitor = new FirmHierarchyGenerationVisitor(hierarchy);
		ast.accept(hierarchyGenerationVisitor);

		FirmGenerationVisitor firmVisitor = new FirmGenerationVisitor(hierarchy);
		ast.accept(firmVisitor);
	}

}
