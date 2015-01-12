package compiler.semantic.symbolTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.LinkedList;

import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;
import compiler.lexer.TokenType;

public class SymbolTableTest {
	private SymbolTable symbolTable = new SymbolTable(1);
	private StringTable stringTable = new StringTable();

	@Test
	public void testSymbolTable() throws Exception {
		symbolTable.enterScope();
		assertTrue(enterDeclaration(getSymbol("number"), BasicType.INT));

		assertFalse(enterDeclaration(getSymbol("number"), BasicType.INT));
		assertFalse(enterDeclaration(getSymbol("number"), BasicType.INT));

		assertTrue(enterDeclaration(getSymbol("number1"), BasicType.INT));

		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertFalse(symbolTable.isDefinedInCurrentScope(getSymbol("asdf")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));

		symbolTable.leaveScope();
		assertEquals(2, symbolTable.getRequiredLocalVariables());

		Field privChangeStack = SymbolTable.class.getDeclaredField("changeStack");
		privChangeStack.setAccessible(true);
		@SuppressWarnings("unchecked")
		LinkedList<Change> cs = (LinkedList<Change>) privChangeStack.get(symbolTable);
		assertTrue(cs.isEmpty());
		privChangeStack.setAccessible(false);

		assertNull(getSymbol("number").getDeclarationScope());
		assertNull(getSymbol("number").getDeclaration());

		assertNull(getSymbol("number1").getDeclarationScope());
		assertNull(getSymbol("number1").getDeclaration());

		symbolTable.enterScope();

		assertTrue(enterDeclaration(getSymbol("number"), BasicType.INT));

		symbolTable.leaveScope();
		assertEquals(2, symbolTable.getRequiredLocalVariables());

		// we are at top level scope: currentScope is null, so this must return true
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));
	}

	@Test
	public void testLeaveAllScopes() throws Exception {
		symbolTable.enterScope();
		symbolTable.enterScope();
		assertTrue(enterDeclaration(getSymbol("number"), BasicType.INT));

		assertFalse(enterDeclaration(getSymbol("number"), BasicType.INT));
		assertFalse(enterDeclaration(getSymbol("number"), BasicType.INT));
		assertTrue(enterDeclaration(getSymbol("number1"), BasicType.INT));

		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertFalse(symbolTable.isDefinedInCurrentScope(getSymbol("asdf")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));

		symbolTable.enterScope();
		assertTrue(enterDeclaration(getSymbol("number"), BasicType.INT));
		symbolTable.enterScope();
		symbolTable.enterScope();

		symbolTable.leaveAllScopes();
		assertEquals(3, symbolTable.getRequiredLocalVariables());

		Field privChangeStack = SymbolTable.class.getDeclaredField("changeStack");
		privChangeStack.setAccessible(true);
		@SuppressWarnings("unchecked")
		LinkedList<Change> cs = (LinkedList<Change>) privChangeStack.get(symbolTable);
		assertTrue(cs.isEmpty());
		privChangeStack.setAccessible(false);

		assertNull(getSymbol("number").getDeclarationScope());
		assertNull(getSymbol("number").getDeclaration());

		assertNull(getSymbol("number1").getDeclarationScope());
		assertNull(getSymbol("number1").getDeclaration());
	}

	private boolean enterDeclaration(Symbol symbol, BasicType basicType) {
		return enterDeclaration(symbol, new Type(null, basicType));
	}

	private boolean enterDeclaration(Symbol symbol, Type type) {
		if (symbolTable.isDefinedInCurrentScope(symbol)) {
			return false;
		}
		symbolTable.insert(symbol, type);
		return true;
	}

	private Symbol getSymbol(String value) {
		return stringTable.insert(value, TokenType.IDENTIFIER).getSymbol();
	}
}
