package compiler.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class FixedTokensSupplierTest {

	private static final String TESTFILE = "./testdata/math/IterativeFibonacci.java";

	@Test
	public void testCompareLexerWithTokenSupplier() throws IOException {
		Lexer lexer = TestUtils.initLexer(Paths.get(TESTFILE));

		ArrayList<Token> tokenList = new ArrayList<Token>();

		Token token;
		do {
			token = lexer.getNextToken();
			tokenList.add(token);
		} while (token.getType() != TokenType.EOF);

		FixedTokensSupplier fixedTokenSupplier = new FixedTokensSupplier(tokenList.toArray(new Token[0]));

		lexer = TestUtils.initLexer(Paths.get(TESTFILE));

		Token expectedNext;
		do {
			Token expectedLookAhead = lexer.getLookAhead();
			Token expectedLookAhead2 = lexer.getLookAhead();
			expectedNext = lexer.getNextToken();

			Token actualLookAhead = fixedTokenSupplier.getLookAhead();
			Token actualLookAhead2 = fixedTokenSupplier.getLookAhead();
			Token actualNext = fixedTokenSupplier.getNextToken();

			assertEquals(expectedLookAhead, actualLookAhead);
			assertEquals(expectedLookAhead2, actualLookAhead2);
			assertEquals(expectedNext, actualNext);
		} while (expectedNext.getType() != TokenType.EOF);
	}
}
