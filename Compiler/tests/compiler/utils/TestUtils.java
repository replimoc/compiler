package compiler.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.lexer.TokenType;
import compiler.main.CompilerApp;
import compiler.parser.Parser;
import compiler.semantic.SemanticCheckResults;
import compiler.semantic.SemanticChecker;
import compiler.semantic.exceptions.SemanticAnalysisException;

/**
 * Utils class containing utility methods required by the tests.
 * 
 * @author Andreas Eberle
 *
 */
@Ignore
public class TestUtils {
	public static Lexer initLexer(String program, StringTable stringTable) throws IOException {
		return new Lexer(new StringReader(program), stringTable);
	}

	public static Lexer initLexer(String program) throws IOException {
		return initLexer(program, new StringTable());
	}

	public static Lexer initLexer(Path sourceFile, StringTable stringTable) throws IOException {
		return new Lexer(Files.newBufferedReader(sourceFile, StandardCharsets.US_ASCII), stringTable);
	}

	public static Lexer initLexer(Path sourceFile) throws IOException {
		return initLexer(sourceFile, new StringTable());
	}

	public static Parser initParser(String program) throws IOException {
		Lexer lex = new Lexer(new StringReader(program), new StringTable());
		return new Parser(lex);
	}

	/**
	 * Creates a parser and provides it the exact list of tokens given as parameter.
	 * 
	 * @param types
	 * @return
	 * @throws IOException
	 */
	public static Parser initParser(TokenType... types) throws IOException {
		return new Parser(new FixedTokensSupplier(types));
	}

	public static Parser initParser(Path sourceFile) throws IOException {
		Lexer lex = new Lexer(Files.newBufferedReader(sourceFile, StandardCharsets.US_ASCII), new StringTable());
		return new Parser(lex);
	}

	public static compiler.ast.Program getAstForFile(String fileName) throws Exception {
		Lexer lexer = new Lexer(Files.newBufferedReader(Paths.get(fileName), StandardCharsets.US_ASCII), new StringTable());
		Parser parser = new Parser(lexer);
		compiler.ast.Program program = parser.parse();
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(program);
		if (semanticResult.hasErrors()) {
			for (SemanticAnalysisException error : semanticResult.getExceptions()) {
				error.printStackTrace();
			}
			throw new Exception("program is not semantically correct");
		}

		return program;
	}

	public static void assertLinesEqual(Path sourceFile, List<String> expected, Iterator<String> actualIter) {
		int line = 1;
		Iterator<String> expectedIter = expected.iterator();
		while (expectedIter.hasNext() && actualIter.hasNext()) {
			assertEquals("Error in file: " + sourceFile + " in line: " + line, expectedIter.next(), actualIter.next());
			line++;
		}
		if (actualIter.hasNext()) {
			Assert.fail("Error in file: " + sourceFile + " : extra line in actual file: " + actualIter.next());
		}

		if (expectedIter.hasNext()) {
			Assert.fail("Error in file: " + sourceFile + " : extra line in actual file: " + expectedIter.next());
		}
	}

	public static Pair<Integer, List<String>> startCompilerApp(String... args) throws IOException {
		String classpath = System.getProperty("java.class.path");
		List<String> arguments = new ArrayList<String>();
		arguments.add("java");
		arguments.add("-cp");
		arguments.add(classpath);
		arguments.add(CompilerApp.class.getName());
		for (String arg : args) {
			arguments.add(arg);
		}
		return Utils.runProcessBuilder(new ProcessBuilder(arguments));
	}
}
