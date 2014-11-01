package compiler.utils;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Ignore;

import compiler.StringTable;
import compiler.lexer.Lexer;

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
}
