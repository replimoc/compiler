package compiler.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import compiler.Symbol;
import compiler.lexer.Position;
import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class ParserExceptionTest {

	@Test
	public void testGetMessage() {
		ParserException ex = new ParserException(new Token(TokenType.IDENTIFIER, new Position(1, 2), new Symbol("test1")), TokenType.INT);
		assertEquals(ex.getMessage(), ex.toString());
	}

	@Test
	public void testGetMessageWithNullValues() {
		ParserException ex = new ParserException(null);
		assertEquals(ex.getMessage(), ex.toString());
	}

	@Test
	public void testGetTokens() {
		Token token = new Token(TokenType.IDENTIFIER, new Position(1, 2), new Symbol("test1"));
		ParserException ex = new ParserException(token, TokenType.INT);

		assertEquals(token, ex.getInvalidToken());
		assertEquals(TokenType.INT, ex.getExpectedTokenType());
	}
}
