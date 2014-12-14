package compiler.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
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
import compiler.Symbol;
import compiler.ast.Program;
import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.main.CompilerApp;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
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
		return initParser(program, new StringTable());
	}

	public static Parser initParser(String program, StringTable stringTable) throws IOException {
		Lexer lex = new Lexer(new StringReader(program), stringTable);
		return new Parser(lex);
	}

	public static Program parse(String input) throws IOException, ParsingFailedException {
		try {
			Parser parser = initParser(input);
			Program ast = parser.parse();
			return ast;
		} catch (ParsingFailedException ex) {
			ex.printParserExceptions();
			throw ex;
		}
	}

	public static SemanticCheckResults checkSemantic(String program) throws IOException, ParsingFailedException {
		StringTable stringTable = new StringTable();
		Parser parser = TestUtils.initParser(program, stringTable);
		return SemanticChecker.checkSemantic(parser.parse(), stringTable);
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
		StringTable stringTable = new StringTable();
		Lexer lexer = new Lexer(Files.newBufferedReader(Paths.get(fileName), StandardCharsets.US_ASCII), stringTable);
		Parser parser = new Parser(lexer);
		compiler.ast.Program program = parser.parse();
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(program, stringTable);
		if (semanticResult.hasErrors()) {
			for (SemanticAnalysisException error : semanticResult.getExceptions()) {
				error.printStackTrace();
			}
			throw new Exception("program is not semantically correct");
		}

		return program;
	}

	public static void assertLinesEqual(Path sourceFile, List<String> expected, Iterator<String> actualIter) {
		assertLinesEqual(sourceFile, expected.iterator(), actualIter);
	}

	public static void assertLinesEqual(Path sourceFile, Iterator<String> expectedIter, Iterator<String> actualIter) {
		int line = 1;
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

	public static File writeToTemporaryFile(CharSequence buffer) throws IOException {
		File file = File.createTempFile("testfile", "");
		file.deleteOnExit();
		writeToFile(file.getAbsolutePath(), buffer);
		return file;
	}

	public static void writeToFile(String filename, CharSequence buffer) throws IOException {
		File file = new File(filename);
		FileWriter output = new FileWriter(file, false);
		output.append(buffer);
		output.close();
		output = null;
		file = null;
	}

	public static void assertTokenEquals(Token token1, Token token2) {
		assertEquals(token1.getPosition(), token2.getPosition());
		assertEquals(token1.getType(), token2.getType());
		assertSymbolEquals(token1.getSymbol(), token2.getSymbol());
	}

	public static void assertSymbolEquals(Symbol symbol1, Symbol symbol2) {
		String value1 = null;
		String value2 = null;
		if (symbol1 != null)
			value1 = symbol1.getValue();
		if (symbol2 != null)
			value2 = symbol2.getValue();
		assertEquals(value1, value2);
	}

}
