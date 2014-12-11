package compiler.semantic;

import java.util.HashMap;
import java.util.List;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.AstNode;
import compiler.ast.ClassDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.PrintMethodDeclaration;
import compiler.ast.StaticFieldDeclaration;
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

		// create System class if 'System' is already defined
		if (!classScopes.containsKey(systemSymbol)) {
			// create PrintStream class
			Symbol printStreamName = getRandomName(stringTable, classScopes, "PrintStream");
			ClassDeclaration printStream = new ClassDeclaration(
					printStreamName,
					new PrintMethodDeclaration(getSymbol(stringTable, "println"),
							new Type(BasicType.VOID),
							new ParameterDefinition(new Type(BasicType.INT), getSymbol(stringTable, "arg0"))));
			printStream.accept(preAnalysisVisitor);

			ClassDeclaration system = new ClassDeclaration(
					systemSymbol,
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
