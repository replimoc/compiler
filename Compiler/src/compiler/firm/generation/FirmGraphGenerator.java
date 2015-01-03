package compiler.firm.generation;

import java.util.HashMap;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.semantic.ClassScope;

public final class FirmGraphGenerator {

	private FirmGraphGenerator() {
	}

	public static void transformToFirm(AstNode ast, HashMap<Symbol, ClassScope> classScopes) {
		FirmGenerationVisitor firmVisitor = new FirmGenerationVisitor();
		firmVisitor.initialize(classScopes);
		ast.accept(firmVisitor);
	}

}
