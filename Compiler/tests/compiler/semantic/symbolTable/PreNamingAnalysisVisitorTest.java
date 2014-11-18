package compiler.semantic.symbolTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Test;

import compiler.StringTable;
import compiler.Symbol;
import compiler.ast.ClassDeclaration;
import compiler.ast.Program;
import compiler.lexer.TokenType;
import compiler.semantic.ClassScope;
import compiler.semantic.PreNamingAnalysisVisitor;

public class PreNamingAnalysisVisitorTest {

	private final PreNamingAnalysisVisitor visitor = new PreNamingAnalysisVisitor();
	private final StringTable stringTable = new StringTable();

	@Test
	public void testEmptyProgram() {
		Program program = new Program(null);
		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(0, classes.size());
	}

	@Test
	public void testSingleClass() {
		Program program = new Program(null);
		Symbol class1 = s("class1");
		program.addClassDeclaration(new ClassDeclaration(null, class1));

		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(1, classes.size());
		ClassScope class1Scope = classes.get(class1);
		assertNotNull(class1Scope);
		assertEquals(0, class1Scope.getNumberOfFields());
		assertEquals(0, class1Scope.getNumberOfMethods());
	}

	@Test
	public void testDoubleClassDefinition() {
		Program program = new Program(null);
		Symbol class1 = s("class1");
		program.addClassDeclaration(new ClassDeclaration(null, class1));
		program.addClassDeclaration(new ClassDeclaration(null, class1));

		program.accept(visitor);

		HashMap<Symbol, ClassScope> classes = visitor.getClassScopes();
		assertEquals(1, classes.size());
		ClassScope class1Scope = classes.get(class1);
		assertNotNull(class1Scope);
		assertEquals(0, class1Scope.getNumberOfFields());
		assertEquals(0, class1Scope.getNumberOfMethods());

		assertEquals(1, visitor.getExceptions().size());
	}

	private Symbol s(String string) {
		return stringTable.insert(string, TokenType.IDENTIFIER).getSymbol();
	}
}
