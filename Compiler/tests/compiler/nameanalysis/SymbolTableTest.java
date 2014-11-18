package compiler.nameanalysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
	public void testSymbolTable() {
		symbolTable.enterScope();
		assertTrue(enterDefinition(getSymbol("number"), new Definition("int")));
		
		assertFalse(enterDefinition(getSymbol("number"), new Definition("int")));
		assertFalse(enterDefinition(getSymbol("number"), new Definition("size")));
		
		assertTrue(enterDefinition(getSymbol("number1"), new Definition("size")));
		
		symbolTable.leaveScope();
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
