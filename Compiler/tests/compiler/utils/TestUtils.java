package compiler.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;

import compiler.StringTable;
import compiler.lexer.Lexer;
import compiler.lexer.TokenType;
import compiler.parser.Parser;
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
		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(program);
		if (errors.size() != 0) {
			for (SemanticAnalysisException error : errors) {
				error.printStackTrace();
			}
			throw new Exception("program is not semantically correct");
		}

		return program;
	}
}
