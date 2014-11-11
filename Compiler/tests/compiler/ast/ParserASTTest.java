package compiler.ast;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import compiler.parser.Parser;
import compiler.parser.ParserException;
import compiler.utils.TestUtils;

public class ParserASTTest {

	@Test
	public void testAST() throws IOException, ParserException {
		Parser parser;

		parser = TestUtils.initParser("");
		parser.parse();
		Program ast = parser.getAST();
		assertTrue(ast.getClasses().isEmpty());
		
		parser = TestUtils.initParser("class Class {}");
		parser.parse();
		ast = parser.getAST();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getIdentifier().getValue().equals("Class"));
		
		parser = TestUtils.initParser("class Class { public void function(int param) {} }");
		parser.parse();
		ast = parser.getAST();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		//assertTrue(ast.getClasses().get(0).getMembers().get(0).);
		
		
		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB) {} }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB, int[] paramC, int[][] paramD) {} }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function() { {} } }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function() { ; ; {} } }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void asdf; public asdf asfd; }");
		parser.parse();

		parser = TestUtils.initParser("class Loops {public static void main ( String[] args){int a; int b; int c; int d;}}");
		parser.parse();

		parser = TestUtils.initParser("class Class { public int[] list; }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public static void main (String[] args) { int[] asdf = 0; } }");
		parser.parse();

		/*
		 * parser = TestUtils.initParser("class Class { public void function() { if () {} } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { while () {} } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { return; } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { int asdf = ; } }"); parser.parse();
		 */
	}
}
