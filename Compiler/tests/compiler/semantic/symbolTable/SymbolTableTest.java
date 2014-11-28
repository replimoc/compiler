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
import compiler.ast.AstNode;
import compiler.ast.type.BasicType;
import compiler.ast.type.Type;
import compiler.lexer.TokenType;

public class SymbolTableTest {
	private SymbolTable symbolTable = new SymbolTable();
	private StringTable stringTable = new StringTable();

	@Test
	public void testSymbolTable() throws Exception {
		symbolTable.enterScope();
		assertTrue(enterDefinition(getSymbol("number"), BasicType.INT, null));

		assertFalse(enterDefinition(getSymbol("number"), BasicType.INT, null));
		assertFalse(enterDefinition(getSymbol("number"), BasicType.INT, null));

		assertTrue(enterDefinition(getSymbol("number1"), BasicType.INT, null));

		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertFalse(symbolTable.isDefinedInCurrentScope(getSymbol("asdf")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));

		symbolTable.leaveScope();

		Field privChangeStack = SymbolTable.class.getDeclaredField("changeStack");
		privChangeStack.setAccessible(true);
		@SuppressWarnings("unchecked")
		LinkedList<Change> cs = (LinkedList<Change>) privChangeStack.get(symbolTable);
		assertTrue(cs.isEmpty());
		privChangeStack.setAccessible(false);

		assertEquals(getSymbol("number").getDefinitionScope(), null);
		assertEquals(getSymbol("number").getDefinition(), null);

		assertEquals(getSymbol("number1").getDefinitionScope(), null);
		assertEquals(getSymbol("number1").getDefinition(), null);

		symbolTable.enterScope();

		assertTrue(enterDefinition(getSymbol("number"), BasicType.INT, null));

		symbolTable.leaveScope();

		// we are at top level scope: currentScope is null, so this must return true
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));
	}

	@Test
	public void testLeaveAllScopes() throws Exception {
		symbolTable.enterScope();
		symbolTable.enterScope();
		assertTrue(enterDefinition(getSymbol("number"), BasicType.INT, null));

		assertFalse(enterDefinition(getSymbol("number"), BasicType.INT, null));
		assertFalse(enterDefinition(getSymbol("number"), BasicType.INT, null));
		assertTrue(enterDefinition(getSymbol("number1"), BasicType.INT, null));

		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertFalse(symbolTable.isDefinedInCurrentScope(getSymbol("asdf")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));

		symbolTable.enterScope();
		assertTrue(enterDefinition(getSymbol("number"), BasicType.INT, null));
		symbolTable.enterScope();
		symbolTable.enterScope();

		symbolTable.leaveAllScopes();

		Field privChangeStack = SymbolTable.class.getDeclaredField("changeStack");
		privChangeStack.setAccessible(true);
		@SuppressWarnings("unchecked")
		LinkedList<Change> cs = (LinkedList<Change>) privChangeStack.get(symbolTable);
		assertTrue(cs.isEmpty());
		privChangeStack.setAccessible(false);

		assertNull(getSymbol("number").getDefinitionScope());
		assertNull(getSymbol("number").getDefinition());

		assertNull(getSymbol("number1").getDefinitionScope());
		assertNull(getSymbol("number1").getDefinition());
	}

	private boolean enterDefinition(Symbol symbol, BasicType basicType, AstNode node) {
		return enterDefinition(symbol, new Type(null, basicType), node);
	}

	private boolean enterDefinition(Symbol symbol, Type type, AstNode node) {
		if (symbolTable.isDefinedInCurrentScope(symbol)) {
			return false;
		}
		symbolTable.insert(symbol, new Definition(symbol, type, node));
		return true;
	}

	private Symbol getSymbol(String value) {
		return stringTable.insert(value, TokenType.IDENTIFIER).getSymbol();
	}
}
