package compiler.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void testHashCode() {
		Token t11 = new Token(TokenType.ABSTRACT, new Position(1, 1));
		Token t12 = new Token(TokenType.ABSTRACT, new Position(1, 1));
		Token t13 = new Token(TokenType.ABSTRACT, null);
		Token t14 = new Token(TokenType.ABSTRACT, null, new Symbol("s"));
		Token t15 = new Token(TokenType.ABSTRACT, null, new Symbol("t"));
		Token t2 = new Token(TokenType.EQUAL, null);
		Token tNull = new Token(null, null);

		assertEquals(t11.hashCode(), t11.hashCode());
		assertEquals(t11.hashCode(), t12.hashCode());
		assertNotEquals(t13.hashCode(), t12.hashCode());
		assertNotEquals(t13.hashCode(), t14.hashCode());
		assertNotEquals(t13.hashCode(), t15.hashCode());
		assertNotEquals(t11.hashCode(), t2.hashCode());
		assertNotEquals(t11.hashCode(), t2.hashCode());
		assertNotEquals(tNull.hashCode(), t2.hashCode());
	}

	@Test
	public void testEquals() {
		Token s11 = new Token(TokenType.ABSTRACT, null);
		Token s12 = new Token(TokenType.ABSTRACT, null);
		Token s2 = new Token(TokenType.EQUAL, null);

		assertEquals(s11, s11);
		assertEquals(s11, s12);
		assertNotEquals(s11, s2);
		assertNotEquals(s11, s2);
	}

	@Test
	public void testEqualsCornerCasesTokenType() {
		Token t = new Token(TokenType.PACKAGE, null);

		assertFalse(t.equals(null));
		assertFalse(t.equals("test"));
		assertFalse(t.equals(new Token(null, null)));
		assertTrue(new Token(null, null).equals(new Token(null, null)));
		assertFalse(new Token(null, null).equals(t));
	}

	@Test
	public void testEqualsCornerCasesPosition() {
		Token t = new Token(null, new Position(1, 2));

		assertFalse(t.equals(null));
		assertFalse(t.equals("test"));
		assertFalse(t.equals(new Token(null, null)));
		assertTrue(new Token(null, null).equals(new Token(null, null)));
		assertFalse(new Token(null, null).equals(t));
	}

	@Test
	public void testEqualsCornerCasesSymbol() {
		Token t = new Token(null, null, new Symbol("s"));

		assertFalse(t.equals(null));
		assertFalse(t.equals("test"));
		assertFalse(t.equals(new Token(null, null)));
		assertTrue(new Token(null, null).equals(new Token(null, null)));
		assertFalse(new Token(null, null).equals(t));
	}
}
