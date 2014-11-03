package compiler.parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import compiler.parser.Parser.ParserException;
import compiler.utils.TestUtils;

public class ParserTest {
	
	@Test
	public void testFirstProductions() throws IOException, ParserException {
		Parser parser = TestUtils.initParser("");
		parser.parse();
		
		parser = TestUtils.initParser("class Class {}");
		parser.parse();
		
		parser = TestUtils.initParser("class ClassA {} class ClassB {}");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void field; }");
		parser.parse();
		
		parser = TestUtils.initParser("class Class { public static void main ( String [] args ) {} }");
		parser.parse();
		
		parser = TestUtils.initParser("class Class { public void function() {} }");
		parser.parse();
		
		parser = TestUtils.initParser("class Class { public void function(int param) {} }");
		parser.parse();
		
		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB) {} }");
		parser.parse();
		
		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB, int[] paramC, int[][] paramD) {} }");
		parser.parse();
		
		assertTrue(true);
	}
}
