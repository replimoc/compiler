package compiler.nameanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.lexer.TokenType;

public class SymbolTableTest {
	private SymbolTable symbolTable;
	private StringTable stringTable;
	
	@Before
	public void setUp() {
		symbolTable = new SymbolTable(null);
		stringTable = new StringTable();
	}
	
	@Test
	public void testSymbolTable() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		symbolTable.enterScope();
		assertTrue(enterDefinition(getSymbol("number"), new Definition("int")));
		
		assertFalse(enterDefinition(getSymbol("number"), new Definition("int")));
		assertFalse(enterDefinition(getSymbol("number"), new Definition("size")));
		
		assertTrue(enterDefinition(getSymbol("number1"), new Definition("size")));
		
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertFalse(symbolTable.isDefinedInCurrentScope(getSymbol("asdf")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));
		
		symbolTable.leaveScope();
		
		Field privChangeStack = SymbolTable.class.getDeclaredField("changeStack");
		privChangeStack.setAccessible(true);
		Stack<Change> cs = (Stack<Change>) privChangeStack.get(symbolTable);
		assertTrue(cs.empty());
		privChangeStack.setAccessible(false);
		
		assertEquals(getSymbol("number").getDefinitionScope(), null);
		assertEquals(getSymbol("number").getDefinition(), null);
		
		assertEquals(getSymbol("number1").getDefinitionScope(), null);
		assertEquals(getSymbol("number1").getDefinition(), null);
		
		symbolTable.enterScope();
		
		assertTrue(enterDefinition(getSymbol("number"), new Definition("int")));
		
		symbolTable.leaveScope();
		
		// we are at top level scope: currentScope is null, so this must return true
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number")));
		assertTrue(symbolTable.isDefinedInCurrentScope(getSymbol("number1")));
	}
	
	private boolean enterDefinition(Symbol symbol, Definition def) {
		if (symbolTable.isDefinedInCurrentScope(symbol)) {
			return false;
		}
		StringTable.Entry se = stringTable.insert("number", TokenType.IDENTIFIER);
		se.getSymbol();
		symbolTable.insert(symbol, def);
		return true;
	}	
	
	private Symbol getSymbol(String value) {
		return stringTable.insert(value, TokenType.IDENTIFIER).getSymbol();
	}
}
