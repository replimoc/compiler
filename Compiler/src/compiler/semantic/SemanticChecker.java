package compiler.semantic;

import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.semantic.exceptions.SemanticAnalysisException;

public final class SemanticChecker {
	private SemanticChecker() { // no objects of this class shall be created.
	}

	public static List<SemanticAnalysisException> checkSemantic(AstNode ast) {
		// run first analysis of class, field and method declarations.
		PreNamingAnalysisVisitor preAnalysisVisitor = new PreNamingAnalysisVisitor();
		ast.accept(preAnalysisVisitor);
		HashMap<Symbol, ClassScope> classScopes = preAnalysisVisitor.getClassScopes();
		List<SemanticAnalysisException> exceptions = preAnalysisVisitor.getExceptions();

		// run full semantic check
		DeepCheckingVisitor deepCheckingVisitor = new DeepCheckingVisitor(classScopes);
		ast.accept(deepCheckingVisitor);
		exceptions.addAll(deepCheckingVisitor.getExceptions());

		return exceptions;
	}
}
