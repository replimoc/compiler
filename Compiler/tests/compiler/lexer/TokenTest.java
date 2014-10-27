package compiler.lexer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TokenTest {

	@Test
	public void testToString() {
		assertEquals("line   2, character  4: abstract", new Token(TokenType.ABSTRACT, new Position(2, 4)).toString());
		assertEquals("line 102, character 94: identifier main", new Token(TokenType.IDENTIFIER, new Position(102, 94), "main").toString());
		assertEquals("line   2, character  4: integer 2342342", new Token(TokenType.INTEGER, new Position(2, 4), "2342342").toString());
		assertEquals("line   2, character  4: error you idiot screwed it up", new Token(TokenType.ERROR, new Position(2, 4),
				"you idiot screwed it up").toString());
		assertEquals("null: =", new Token(TokenType.EQUAL, null).toString());
	}

	@Test
	public void testgetTokenString() {
		assertEquals("abstract", new Token(TokenType.ABSTRACT, new Position(2, 4)).getTokenString());
		assertEquals("identifier main", new Token(TokenType.IDENTIFIER, null, "main").getTokenString());
		assertEquals("integer 2342342", new Token(TokenType.INTEGER, null, "2342342").getTokenString());
		assertEquals("error you idiot screwed it up", new Token(TokenType.ERROR, null,
				"you idiot screwed it up").getTokenString());
		assertEquals("=", new Token(TokenType.EQUAL, null).getTokenString());
	}
}
