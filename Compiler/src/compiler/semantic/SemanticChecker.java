package compiler.semantic;

import java.util.HashMap;
import java.util.List;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.Declaration;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.PrintMethodDeclaration;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.lexer.Position;
import compiler.lexer.TokenType;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.semantic.symbolTable.Scope;

public final class SemanticChecker {
	private SemanticChecker() { // no objects of this class shall be created.
	}

	public static SemanticCheckResults checkSemantic(AstNode ast, StringTable stringTable) {
		// run first analysis of class, field and method declarations.
		PreNamingAnalysisVisitor preAnalysisVisitor = new PreNamingAnalysisVisitor();
		ast.accept(preAnalysisVisitor);
		HashMap<Symbol, ClassScope> classScopes = preAnalysisVisitor.getClassScopes();
		List<SemanticAnalysisException> exceptions = preAnalysisVisitor.getExceptions();

		// fill System.out.println: if System class isn't present
		Symbol systemSymbol = getSymbol(stringTable, "System", TokenType.CLASS);
		FieldDeclaration systemDefinition = null;

		// create System class if 'System' is already defined
		if (classScopes.containsKey(systemSymbol)) {
			int systemSymbolNum = 0;
			do {
				systemSymbol = getSymbol(stringTable, "System" + systemSymbolNum, TokenType.CLASS);
			} while (classScopes.containsKey(systemSymbol));
		}

		// create PrintStream class
		int printStreamNum = 0;

		Symbol printStream;
		do {
			printStream = getSymbol(stringTable, "PrintStream" + printStreamNum, TokenType.CLASS);
		} while (classScopes.containsKey(printStream));

		FieldDeclaration printStreamDefinition = new FieldDeclaration(new ClassType(new Position(-1, -1), printStream), printStream);

		HashMap<Symbol, MethodDeclaration> psMethods = new HashMap<Symbol, MethodDeclaration>();
		Symbol printLineSymbol = getSymbol(stringTable, "println", TokenType.IDENTIFIER);
		MethodDeclaration printLineMethod = new PrintMethodDeclaration(null, printLineSymbol, new Type(null, BasicType.VOID));
		printLineMethod.addParameter(new ParameterDefinition(new Type(null, BasicType.INT), getSymbol(stringTable, "arg", TokenType.IDENTIFIER)));
		psMethods.put(printLineSymbol, printLineMethod);
		ClassScope printStreamScope = new ClassScope(new HashMap<Symbol, Declaration>(), psMethods);
		classScopes.put(printStream, printStreamScope);

		systemDefinition = new FieldDeclaration(new ClassType(null, systemSymbol), systemSymbol);
		systemSymbol.setDefintion(new Scope(null, 0), systemDefinition);
		HashMap<Symbol, Declaration> fields = new HashMap<Symbol, Declaration>();
		fields.put(getSymbol(stringTable, "out", TokenType.IDENTIFIER), printStreamDefinition);
		HashMap<Symbol, MethodDeclaration> methods = new HashMap<Symbol, MethodDeclaration>();
		ClassScope systemClassScope = new ClassScope(fields, methods);
		classScopes.put(systemSymbol, systemClassScope);

		// run full semantic check
		DeepCheckingVisitor deepCheckingVisitor = new DeepCheckingVisitor(classScopes, systemDefinition);
		ast.accept(deepCheckingVisitor);
		exceptions.addAll(deepCheckingVisitor.getExceptions());

		return new SemanticCheckResults(exceptions, classScopes);
	}

	private static Symbol getSymbol(StringTable stringTable, String name, TokenType type) {
		return new Symbol(name);
	}
}
