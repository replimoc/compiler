package compiler.firm.generation;

import java.util.HashMap;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.semantic.ClassScope;

import firm.Graph;
import firm.Program;
import firm.bindings.binding_irgopt;

public final class FirmGraphGenerator {

	private FirmGraphGenerator() {
	}

	public static void transformToFirm(AstNode ast, HashMap<Symbol, ClassScope> classScopes) {
		FirmGenerationVisitor firmVisitor = new FirmGenerationVisitor();
		firmVisitor.initialize(classScopes);
		ast.accept(firmVisitor);

		for (Graph graph : Program.getGraphs()) {
			binding_irgopt.remove_bads(graph.ptr);
			binding_irgopt.remove_unreachable_code(graph.ptr);
		}
	}
}
