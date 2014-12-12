package compiler.semantic.symbolTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDeclaration;
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;
import compiler.lexer.Position;

public class MethodMethodDefinitionTest {

	private final Symbol s = new Symbol("a");
	private final Type t = new Type(null, BasicType.INT);
	private final Position p = new Position(0, 0);
	private final ParameterDeclaration[] params = { new ParameterDeclaration(p, t, s) };

	@Test
	public void testEqualsAndHashCode3() {
		MethodDeclaration s1 = new MethodDeclaration(s, null, null);
		MethodDeclaration s2 = new MethodDeclaration(s, null, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode4() {
		MethodDeclaration s1 = new MethodDeclaration(s, t, null);
		MethodDeclaration s2 = new MethodDeclaration(s, null, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode5() {
		MethodDeclaration s1 = new MethodDeclaration(s, t, null);
		MethodDeclaration s2 = new MethodDeclaration(s, t, null);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode8() {
		MethodDeclaration s1 = new MethodDeclaration(s, t, params);
		MethodDeclaration s2 = new MethodDeclaration(s, t, null);

		assertNotEquals(s1, s2);
		assertNotEquals(s2, s1);
	}

	@Test
	public void testEqualsAndHashCode9() {
		MethodDeclaration s1 = new MethodDeclaration(s, t, params);
		MethodDeclaration s2 = new MethodDeclaration(s, t, params);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}
}
