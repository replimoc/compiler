package compiler.ast;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import compiler.ast.type.ClassType;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.utils.TestUtils;

public class ParserASTTest {

	@Test
	public void testAST() throws IOException, ParsingFailedException {
		Parser parser;

		parser = TestUtils.initParser("");

		Program ast = parser.parse();
		assertTrue(ast.getClasses().isEmpty());

		parser = TestUtils.initParser("class Class {}");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getIdentifier().getValue().equals("Class"));

		parser = TestUtils.initParser("class Class { public void function(int param) {} }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		MethodDeclaration func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 1);
		assertTrue(func.getParameters().get(0).getIdentifier().getValue().equals("param"));

		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB) {} }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 2);
		assertTrue(func.getParameters().get(0).getIdentifier().getValue().equals("paramA"));
		assertTrue(func.getParameters().get(1).getIdentifier().getValue().equals("paramB"));

		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB, int[] paramC, int[][] paramD) {} }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 4);
		assertTrue(func.getParameters().get(2).getIdentifier().getValue().equals("paramC"));
		assertTrue(func.getParameters().get(3).getIdentifier().getValue().equals("paramD"));

		parser = TestUtils.initParser("class Class { public void function() { {} } }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 0);
		assertTrue(func.getBlock().getStatements().size() == 1);
		Block blockStmt = (Block) func.getBlock().getStatements().get(0);
		assertTrue(blockStmt.getStatements().isEmpty());

		parser = TestUtils.initParser("class Class { public void function() { ; ; {} } }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 0);
		assertTrue(func.getBlock().getStatements().size() == 1);
		blockStmt = (Block) func.getBlock().getStatements().get(0);
		assertTrue(blockStmt.getStatements().isEmpty());

		parser = TestUtils.initParser("class Class { public void asdf; public asdf asdf; }");
		ast = parser.parse();
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().size() == 2);
		FieldDeclaration fldDecl = (FieldDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(fldDecl.getIdentifier().getValue().equals("asdf"));
		fldDecl = (FieldDeclaration) ast.getClasses().get(0).getMembers().get(1); // DOWNCAST! - Yeah
		assertTrue(fldDecl.getIdentifier().getValue().equals("asdf"));
		assertTrue(((ClassType) fldDecl.getType()).getIdentifier().getValue().equals("asdf"));

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
