package compiler.semantic.symbolTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;

public class MethodMethodDefinitionTest {

	private final Symbol s = new Symbol("a");
	private final Type t = new Type(null, BasicType.INT);
	private final Definition[] params = new Definition[] { new Definition(s, t) };

	@Test
	public void testEqualsAndHashCode0() {
		MethodDefinition s1 = new MethodDefinition(null, null, null);

		assertEquals(s1, s1);
		assertEquals(s1.hashCode(), s1.hashCode());
	}

	@Test
	public void testEqualsAndHashCodeForDefinition() {
		Definition s1 = new Definition(null, null);

		assertEquals(s1, s1);
		assertEquals(s1.hashCode(), s1.hashCode());
	}

	@Test
	public void testEqualsAndHashCode1() {
		MethodDefinition s1 = new MethodDefinition(null, null, null);
		MethodDefinition s2 = new MethodDefinition(null, null, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode2() {
		MethodDefinition s1 = new MethodDefinition(s, null, null);
		MethodDefinition s2 = new MethodDefinition(null, null, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode3() {
		MethodDefinition s1 = new MethodDefinition(s, null, null);
		MethodDefinition s2 = new MethodDefinition(s, null, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode4() {
		MethodDefinition s1 = new MethodDefinition(s, t, null);
		MethodDefinition s2 = new MethodDefinition(s, null, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode5() {
		MethodDefinition s1 = new MethodDefinition(s, t, null);
		MethodDefinition s2 = new MethodDefinition(s, t, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode8() {
		MethodDefinition s1 = new MethodDefinition(s, t, params);
		MethodDefinition s2 = new MethodDefinition(s, t, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode9() {
		MethodDefinition s1 = new MethodDefinition(s, t, params);
		MethodDefinition s2 = new MethodDefinition(s, t, params);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode6() {
		MethodDefinition s1 = new MethodDefinition(null, null, null);

		assertFalse(s1.equals(null));
	}

	@Test
	public void testEqualsAndHashCode7() {
		MethodDefinition s1 = new MethodDefinition(null, null, null);

		assertFalse(s1.equals(""));
	}
}
