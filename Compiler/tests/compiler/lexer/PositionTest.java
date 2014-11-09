package compiler.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import compiler.Symbol;

public class PositionTest {

	@Test
	public void testHashCode() {
		Position p11 = new Position(1, 2);
		Position p12 = new Position(1, 2);
		Position p2 = new Position(3, 4);

		assertEquals(p11.hashCode(), p12.hashCode());
		assertNotEquals(p11.hashCode(), p2.hashCode());
	}

	@Test
	public void testEquals() {
		Position p11 = new Position(1, 2);
		Position p12 = new Position(1, 2);
		Position p2 = new Position(3, 4);
		Position p3 = new Position(3, 2);
		Position p4 = new Position(1, 4);

		assertEquals(p11, p11);
		assertEquals(p11, p12);
		assertNotEquals(p11, p2);
		assertNotEquals(p2, p3);
		assertNotEquals(p2, p4);
	}

	@Test
	public void testEqualsCornerCases() {
		Position p = new Position(1, 2);

		assertFalse(p.equals(null));
		assertFalse(p.equals("test"));
		assertFalse(p.equals(new Symbol(null)));
		assertTrue(new Symbol(null).equals(new Symbol(null)));
		assertFalse(new Symbol(null).equals(p));
	}
}
