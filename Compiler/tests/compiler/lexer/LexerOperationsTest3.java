package compiler.lexer;

import org.junit.Assert;
import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * Test case for errors in operators that should be parser errors, like %==
 *
 * @author effenok
 */
public class LexerOperationsTest3 {

	@Test
	public void testSomeOps() throws Exception {
		String[] testStrings = { "%==", "%==***=+0", "a===b-c--d", "a%==/**/--%=",
				"--a--b-c--++--", "{{[[+]=======-}!?!?!?~~" };
		for (String testString : testStrings) {
			testNoError(testString);
		}
	}

	private void testNoError(String testString) throws Exception {
		Lexer lexer = TestUtils.initLexer(testString);
		Token tok;
		while ((tok = lexer.getNextToken()) != null)
		{
			Assert.assertNotEquals(TokenType.ERROR, tok.getType());
		}
	}
}
