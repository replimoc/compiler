package compiler;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.utils.TestUtils;

public class SpeedTests {
	@Test
	public void testLexAllTokensSpeed() throws IOException {
		long startInit = System.currentTimeMillis();
		Lexer lexer = TestUtils.initLexer(Paths.get("./testdata/speedtests/AllTokensSpeed.java"));

		long startLexing = System.currentTimeMillis();
		Token token;
		do {
			token = lexer.getNextToken();
		} while (token.getType() != TokenType.EOF);

		long end = System.currentTimeMillis();

		System.out.println("init: " + (startLexing - startInit) + "ms");
		System.out.println("lexing: " + (end - startLexing) + "ms");
		System.out.println("overall: " + (end - startInit) + "ms");
	}
}
