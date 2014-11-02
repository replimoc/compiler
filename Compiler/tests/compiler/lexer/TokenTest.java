package compiler.lexer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import compiler.Symbol;

public class TokenTest {

	@Test
	public void testToString() {
		assertEquals("line   2, character  4: abstract", new Token(TokenType.ABSTRACT, new Position(2, 4)).toString());
		assertEquals("line 102, character 94: identifier main", new Token(TokenType.IDENTIFIER, new Position(102, 94), new Symbol("main")).toString());
		assertEquals("line   2, character  4: integer literal 2342342",
				new Token(TokenType.INTEGER, new Position(2, 4), new Symbol("2342342")).toString());
		assertEquals("line   2, character  4: error you idiot screwed it up", new Token(TokenType.ERROR, new Position(2, 4),
				new Symbol("you idiot screwed it up")).toString());
		assertEquals("null: ==", new Token(TokenType.EQUAL, null).toString());
	}

	@Test
	public void testGetTokenString() {
		assertEquals("abstract", new Token(TokenType.ABSTRACT, new Position(2, 4)).getTokenString());
		assertEquals("identifier main", new Token(TokenType.IDENTIFIER, null, new Symbol("main")).getTokenString());
		assertEquals("integer literal 2342342", new Token(TokenType.INTEGER, null, new Symbol("2342342")).getTokenString());
		assertEquals("error you idiot screwed it up", new Token(TokenType.ERROR, null,
				new Symbol("you idiot screwed it up")).getTokenString());
		assertEquals("==", new Token(TokenType.EQUAL, null).getTokenString());
	}

	@Test
	public void testGetTokenPosition() {
		Position expectedPosition = new Position(2, 4);
		Token token = new Token(TokenType.ABSTRACT, expectedPosition);
		Position position = token.getPosition();
		assertEquals(expectedPosition.getLine(), position.getLine());
		assertEquals(expectedPosition.getCharacter(), position.getCharacter());
	}
}
