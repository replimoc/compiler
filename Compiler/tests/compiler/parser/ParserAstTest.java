package compiler.parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.type.ClassType;
import compiler.parser.ParsingFailedException;
import compiler.utils.TestUtils;

public class ParserAstTest {

	@Test
	public void testAST() throws IOException, ParsingFailedException {
		Program ast = TestUtils.parse("");
		assertTrue(ast.getClasses().isEmpty());

		ast = TestUtils.parse("class Class {}");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getIdentifier().getValue().equals("Class"));

		ast = TestUtils.parse("class Class { public void function(int param) {} }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		MethodDeclaration func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 1);
		assertTrue(func.getParameters().get(0).getIdentifier().getValue().equals("param"));

		ast = TestUtils.parse("class Class { public void function(int paramA, void paramB) {} }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 2);
		assertTrue(func.getParameters().get(0).getIdentifier().getValue().equals("paramA"));
		assertTrue(func.getParameters().get(1).getIdentifier().getValue().equals("paramB"));

		ast = TestUtils.parse("class Class { public void function(int paramA, void paramB, int[] paramC, int[][] paramD) {} }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 4);
		assertTrue(func.getParameters().get(2).getIdentifier().getValue().equals("paramC"));
		assertTrue(func.getParameters().get(3).getIdentifier().getValue().equals("paramD"));

		ast = TestUtils.parse("class Class { public void function() { {} } }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 0);
		assertTrue(func.getBlock().getStatements().size() == 1);
		Block blockStmt = (Block) func.getBlock().getStatements().get(0);
		assertTrue(blockStmt.getStatements().isEmpty());

		ast = TestUtils.parse("class Class { public void function() { ; ; {} } }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().isEmpty() == false);
		func = (MethodDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(func.getIdentifier().getValue().equals("function"));
		assertTrue(func.getParameters().size() == 0);
		assertTrue(func.getBlock().getStatements().size() == 1);
		blockStmt = (Block) func.getBlock().getStatements().get(0);
		assertTrue(blockStmt.getStatements().isEmpty());

		ast = TestUtils.parse("class Class { public void asdf; public asdf asdf; }");
		assertTrue(ast.getClasses().size() == 1);
		assertTrue(ast.getClasses().get(0).getMembers().size() == 2);
		FieldDeclaration fldDecl = (FieldDeclaration) ast.getClasses().get(0).getMembers().get(0); // DOWNCAST! - Yeah
		assertTrue(fldDecl.getIdentifier().getValue().equals("asdf"));
		fldDecl = (FieldDeclaration) ast.getClasses().get(0).getMembers().get(1); // DOWNCAST! - Yeah
		assertTrue(fldDecl.getIdentifier().getValue().equals("asdf"));
		assertTrue(((ClassType) fldDecl.getType()).getIdentifier().getValue().equals("asdf"));

		TestUtils.parse("class Loops {public static void main ( String[] args){int a; int b; int c; int d;}}");

		TestUtils.parse("class Class { public int[] list; }");

		TestUtils.parse("class Class { public static void main (String[] args) { int[] asdf = 0; } }");
	}
}
