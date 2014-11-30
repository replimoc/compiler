package compiler.semantic;

import java.util.HashMap;
import java.util.List;

import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.lexer.Position;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.symbolTable.Definition;
import compiler.semantic.symbolTable.MethodDefinition;
import compiler.semantic.symbolTable.Scope;

public final class SemanticChecker {
	private SemanticChecker() { // no objects of this class shall be created.
	}

	public static SemanticCheckResults checkSemantic(AstNode ast) {
		// run first analysis of class, field and method declarations.
		PreNamingAnalysisVisitor preAnalysisVisitor = new PreNamingAnalysisVisitor();
		ast.accept(preAnalysisVisitor);
		HashMap<Symbol, ClassScope> classScopes = preAnalysisVisitor.getClassScopes();
		List<SemanticAnalysisException> exceptions = preAnalysisVisitor.getExceptions();

		// fill System.out.println: if System class isn't present
		Symbol systemSymbol = new Symbol("System");
		if (classScopes.containsKey(systemSymbol) == false) {
			// create PrintStream class
			Symbol printStream = new Symbol("PrintStream");
			Definition printStreamDefinition = new Definition(printStream, new ClassType(new Position(-1, -1), printStream), null);

			HashMap<Symbol, MethodDefinition> psMethods = new HashMap<Symbol, MethodDefinition>();
			Symbol printLineSymbol = new Symbol("println");
			Definition[] definitions = new Definition[1];
			definitions[0] = new Definition(new Symbol("arg"), new Type(null, BasicType.INT), null);
			psMethods.put(printLineSymbol, new MethodDefinition(printLineSymbol, new Type(null, BasicType.VOID), definitions, null));
			ClassScope printStreamScope = new ClassScope(new HashMap<Symbol, Definition>(), psMethods);
			classScopes.put(printStream, printStreamScope);

			systemSymbol.setDefintion(new Scope(null, 0), new Definition(systemSymbol, new ClassType(null, systemSymbol), null));
			HashMap<Symbol, Definition> fields = new HashMap<Symbol, Definition>();
			fields.put(new Symbol("out"), printStreamDefinition);
			HashMap<Symbol, MethodDefinition> methods = new HashMap<Symbol, MethodDefinition>();
			ClassScope systemClassScope = new ClassScope(fields, methods);
			classScopes.put(systemSymbol, systemClassScope);
		}

		// run full semantic check
		DeepCheckingVisitor deepCheckingVisitor = new DeepCheckingVisitor(classScopes);
		ast.accept(deepCheckingVisitor);
		exceptions.addAll(deepCheckingVisitor.getExceptions());

		return new SemanticCheckResults(exceptions, classScopes);
	}
}
