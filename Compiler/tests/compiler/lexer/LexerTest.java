package compiler.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.utils.PrivateMethodCaller;

public class LexerTest {
	private StringTable stringTable;
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Lexer.class);;

	@Before
	public void setUp() throws Exception {
		stringTable = new StringTable();
	}

	@Test
	public void testToken() throws IOException {
		Lexer lexer = new Lexer(getBufferedInputStream(""), stringTable);
		assertEquals(TokenType.AND, ((Token) caller.call("token", lexer, TokenType.AND)).getType());
		assertEquals(TokenType.ASSIGN, ((Token) caller.call("token", lexer, TokenType.ASSIGN)).getType());
		assertEquals(TokenType.ABSTRACT, ((Token) caller.call("token", lexer, TokenType.ABSTRACT)).getType());
		assertEquals(TokenType.BREAK, ((Token) caller.call("token", lexer, TokenType.BREAK)).getType());

		Symbol symbol = new Symbol("Test Symbol");
		assertEquals(TokenType.AND, ((Token) caller.call("token", lexer, TokenType.AND, symbol)).getType());
		assertEquals(TokenType.ASSIGN, ((Token) caller.call("token", lexer, TokenType.ASSIGN, symbol)).getType());
		assertEquals(TokenType.ABSTRACT, ((Token) caller.call("token", lexer, TokenType.ABSTRACT, symbol)).getType());
		assertEquals(TokenType.BREAK, ((Token) caller.call("token", lexer, TokenType.BREAK, symbol)).getType());

		assertEquals(symbol, ((Token) caller.call("token", lexer, TokenType.AND, symbol)).getSymbol());
		assertEquals(symbol, ((Token) caller.call("token", lexer, TokenType.ASSIGN, symbol)).getSymbol());
		assertEquals(symbol, ((Token) caller.call("token", lexer, TokenType.ABSTRACT, symbol)).getSymbol());
		assertEquals(symbol, ((Token) caller.call("token", lexer, TokenType.BREAK, symbol)).getSymbol());
	}

	@Test
	public void testTokenStringTable() throws IOException {
		Lexer lexer = new Lexer(getBufferedInputStream(""), stringTable);

		String testString = "testString";
		Token t1 = caller.call("tokenStringTable", lexer, TokenType.BREAK, testString);
		assertEquals(TokenType.BREAK, t1.getType());
		assertEquals(testString, t1.getSymbol().getValue());
		Token t2 = caller.call("tokenStringTable", lexer, TokenType.ABSTRACT, testString);
		assertEquals(TokenType.BREAK, t2.getType());
		assertEquals(testString, t2.getSymbol().getValue());
		assertTrue(t1.getSymbol().getValue() == t2.getSymbol().getValue());
	}

	@Test
	public void testTokenError() throws IOException {
		Lexer lexer = new Lexer(getBufferedInputStream(""), stringTable);

		String testString = "test error string";
		Token t1 = caller.call("tokenError", lexer, testString);
		assertEquals(TokenType.ERROR, t1.getType());
		assertEquals(testString, t1.getSymbol().getValue());
	}

	@Test
	public void testIsWhitespace() throws IOException {
		boolean bools[] = { true, true, true, true, false, false, true, false, false };
		testMethodAgainstArray(" 	\n\r\fä ∃", "isWhitespace", bools);
	}

	@Test
	public void testIsAZaz_() throws IOException {
		boolean bools[] = { false, false, false, true, true, false, true, false, false, false, true, false };
		testMethodAgainstArray("210azäd231a", "isAZaz_", bools);
	}

	@Test
	public void testIsAZaz_09() throws IOException {
		boolean bools[] = { true, true, true, true, true, false, true, true, true, true, true, false };
		testMethodAgainstArray("210azäd231a", "isAZaz_09", bools);
	}

	@Test
	public void testIs09() throws IOException {
		boolean bools[] = { true, true, true, false, false, false, false, true, true, true, false, false };
		testMethodAgainstArray("210azäd231a", "is09", bools);
	}

	@Test
	public void testIs19() throws IOException {
		boolean bools[] = { true, true, false, false, false, false, false, true, true, true, false, false };
		testMethodAgainstArray("210azäd231a", "is19", bools);
	}

	private void testMethodAgainstArray(final String input, final String method, final boolean bools[]) throws IOException {
		Lexer lexer = new Lexer(getBufferedInputStream(input), stringTable);
		for (boolean b : bools) {
			assertEquals(b, caller.call(method, lexer));
			caller.call("nextChar", lexer);
		}
	}

	private BufferedInputStream getBufferedInputStream(String empty) {
		return new BufferedInputStream(new ByteArrayInputStream(
				empty.getBytes(StandardCharsets.US_ASCII)));
	}
}
