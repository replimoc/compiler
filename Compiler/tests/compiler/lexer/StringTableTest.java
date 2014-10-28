package compiler.lexer;

import static org.junit.Assert.assertEquals;
import compiler.StringTable;
import compiler.Symbol;

import org.junit.Test;

public class StringTableTest {

	@Test
	public void testInsert() {
		StringTable st = new StringTable();
		
		StringTable.Entry entry1 = st.insert("test", null);
		assertEquals("test", entry1.getSymbol().getValue());
		
		StringTable.Entry entry2 = st.insert("test", null);
		assertEquals("test", entry2.getSymbol().getValue());
		
		assertEquals(true, entry1 == entry2);
		
		StringTable.Entry entry3 = st.insert("test1", null);
		assertEquals("test1", entry3.getSymbol().getValue());
		
		assertEquals(false, entry1 == entry3);
		assertEquals(false, entry2 == entry3);
		
		StringTable.Entry  symbEmpty = st.insert("", null);
		assertEquals("", symbEmpty.getSymbol().getValue());
		
		StringTable.Entry  symbNull = st.insert(null, null);
		assertEquals(null, symbNull.getSymbol().getValue());
		
		StringTable.Entry entry4 = st.insert("test", TokenType.IDENTIFIER);
		assertEquals(null, entry4.getType());
		
		StringTable.Entry entry5 = st.insert("test5", TokenType.IDENTIFIER);
		assertEquals(TokenType.IDENTIFIER, entry5.getType());
		
		StringTable.Entry entry6 = st.insert("test6", TokenType.INTEGER);
		assertEquals(TokenType.INTEGER, entry6.getType());
	}
}
