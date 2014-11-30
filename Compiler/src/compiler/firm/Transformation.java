package compiler.firm;

import java.util.HashMap;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.semantic.ClassScope;

public class Transformation {

	private Transformation() {
	}

	public static void transformToFirm(AstNode ast, HashMap<Symbol, ClassScope> classScopes) {
		FirmHierarchy hierarchy = new FirmHierarchy();
		hierarchy.initialize(classScopes);

		FirmGenerationVisitor firmVisitor = new FirmGenerationVisitor(hierarchy);
		ast.accept(firmVisitor);
	}

}
