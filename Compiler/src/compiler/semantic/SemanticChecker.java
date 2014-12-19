package compiler.semantic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.NativeMethodFixedNameDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.declaration.StaticFieldDeclaration;
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;
import compiler.lexer.TokenType;
import compiler.semantic.exceptions.SemanticAnalysisException;

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
		Symbol systemSymbol = getSymbol(stringTable, "System");

		if (!classScopes.containsKey(systemSymbol)) {
			// create PrintStream class
			Symbol printStreamName = getRandomName(stringTable, classScopes, "PrintStream");
			ClassDeclaration printStream = new ClassDeclaration(printStreamName,
					new NativeMethodFixedNameDeclaration(null, "print_int", getSymbol(stringTable, "println"),
							Arrays.asList(new ParameterDeclaration(new Type(BasicType.INT), getSymbol(stringTable, "arg0"))),
							new Type(BasicType.VOID)));
			printStream.accept(preAnalysisVisitor);

			// Create System class
			ClassDeclaration system = new ClassDeclaration(systemSymbol,
					new StaticFieldDeclaration(printStream.getType(), getSymbol(stringTable, "out")));

			system.accept(preAnalysisVisitor);
		}

		// run full semantic check
		DeepCheckingVisitor deepCheckingVisitor = new DeepCheckingVisitor(classScopes);
		ast.accept(deepCheckingVisitor);
		exceptions.addAll(deepCheckingVisitor.getExceptions());

		return new SemanticCheckResults(exceptions, classScopes);
	}

	private static Symbol getSymbol(StringTable stringTable, String name) {
		return stringTable.insert(name, TokenType.IDENTIFIER).getSymbol();
	}

	private static Symbol getRandomName(StringTable stringTable, HashMap<Symbol, ClassScope> classScopes, String prefix) {
		int number = 0;

		Symbol symbol;
		do {
			symbol = getSymbol(stringTable, prefix + number);
			number++;
		} while (classScopes.containsKey(symbol));
		return symbol;
	}
}
