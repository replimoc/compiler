package compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SymbolTest {

	@Test
	public void testHashCode() {
		Symbol s11 = new Symbol("Symbol1");
		Symbol s12 = new Symbol("Symbol1");
		Symbol s2 = new Symbol("Symbol2");
		Symbol sNull = new Symbol(null);

		assertEquals(s11.hashCode(), s12.hashCode());
		assertNotEquals(s11.hashCode(), s2.hashCode());
		assertNotEquals(s11.hashCode(), s2.hashCode());
		assertNotEquals(sNull.hashCode(), s2.hashCode());
	}

	@Test
	public void testEquals() {
		Symbol s11 = new Symbol("Symbol1");
		Symbol s12 = new Symbol("Symbol1");
		Symbol s2 = new Symbol("Symbol2");

		assertEquals(s11, s11);
		assertEquals(s11, s12);
		assertNotEquals(s11, s2);
	}

	@Test
	public void testEqualsCornerCases() {
		Symbol s = new Symbol("Symbol1");

		assertFalse(s.equals(null));
		assertFalse(s.equals("test"));
		assertFalse(s.equals(new Symbol(null)));
		assertTrue(new Symbol(null).equals(new Symbol(null)));
		assertFalse(new Symbol(null).equals(s));
	}
}
