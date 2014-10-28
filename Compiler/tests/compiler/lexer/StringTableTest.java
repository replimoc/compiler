package compiler.lexer;

import static org.junit.Assert.assertEquals;
import compiler.StringTable;
import compiler.Symbol;

import org.junit.Test;

public class StringTableTest {

	@Test
	public void testInsert() {
		StringTable st = new StringTable();
		
		Symbol symb1 = st.insert("test");
		assertEquals("test", symb1.getValue());
		
		Symbol symb2 = st.insert("test");
		assertEquals("test", symb2.getValue());
		
		assertEquals(true, symb1 == symb2);
		
		Symbol symb3 = st.insert("test1");
		assertEquals("test1", symb3.getValue());
		
		assertEquals(false, symb1 == symb3);
		assertEquals(false, symb2 == symb3);
		
		Symbol symbEmpty = st.insert("");
		assertEquals("", symbEmpty.getValue());
		
		Symbol symbNull = st.insert(null);
		assertEquals(null, symbNull.getValue());
	}
}
