package compiler.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import compiler.Symbol;
import compiler.ast.Program;
import compiler.parser.Parser;
import compiler.parser.ParsingFailedException;
import compiler.semantic.exceptions.SemanticAnalysisException;
import compiler.utils.TestUtils;

public class DeepCheckingVisitorTest {

	private HashMap<Symbol, ClassScope> classScopes = new HashMap<Symbol, ClassScope>();
	private final DeepCheckingVisitor visitor = new DeepCheckingVisitor(classScopes);

	@Test
	public void testEmptyProgram() {
		Program program = new Program(null);
		program.accept(visitor);

		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());
	}

	@Test
	public void testFromString() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) {} }");
		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(ClassB param) {} }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param, int param) {} }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { paramA; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { param.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int memberInt; public static void main(String[] args) {} public void function(Class param) { param.memberInt; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { param.memberClass.memberClass.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int param; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int locVarInt; locVarInt.asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { Class locVarClass; locVarClass.memberClass; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { method(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.methodA(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method().asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method() { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method().asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function() { this.asdf; this.method(12,12); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function() { this.asdf3; this.method(12,12).asdf; } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		assertNotNull(errors.get(0));

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { System.out.println(42); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean args = true; System.out.println(args); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
		// assertNotNull((NoSuchMemberException) errors.get(0));

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(false); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(new Class()); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public int m; public void m() { } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class System; public void bla() { System.out.println(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void bla() { this.bla(); } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class classA; public void bla(Class classB) { { Class classB; } } }");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());

		parser = TestUtils
				.initParser("class Main{public static void main(String[] vargs){vargs[5];}}");
		errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(1, errors.size());
	}

	@Test
	public void testSystemOutPrintln() throws IOException, ParsingFailedException {
		Parser parser = TestUtils
				.initParser("class main{public static void main(String[] a){}} class System { public System System; public System out; public void println(){System.out.println();}}");
		List<SemanticAnalysisException> errors = SemanticChecker.checkSemantic(parser.parse());
		assertEquals(0, errors.size());
	}
}
