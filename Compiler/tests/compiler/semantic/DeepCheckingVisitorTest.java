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
	private final DeepCheckingVisitor visitor = new DeepCheckingVisitor(
			classScopes);

	@Test
	public void testEmptyProgram() {
		Program program = new Program(null);
		program.accept(visitor);

		List<SemanticAnalysisException> exceptions = visitor.getExceptions();
		assertEquals(0, exceptions.size());
	}

	@Test
	public void testFromString() throws IOException, ParsingFailedException {
		Parser parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function(Class param) {} }");
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function(ClassB param) {} }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function(Class param, int param) {} }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { paramA; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function(Class param) { param.asdf; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int memberInt; public static void main(String[] args) {} public void function(Class param) { param.memberInt=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { param.memberClass.memberClass.asdf=null; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int param; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { int locVarInt; locVarInt.asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public Class memberClass; public static void main(String[] args) {} public void function(Class param) { Class locVarClass; locVarClass.memberClass=null; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { method(); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method(); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.methodA(); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public void method() {}  public static void main(String[] args) {} public void function(Class param) { param.method().asdf=null; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method() { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method().asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1).asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function(Class param) { param.method(1, 1, 1).asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function() { this.asdf=1; this.method(12,12); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public int asdf; public Class method(int a, int b) { return null; }  public static void main(String[] args) {} public void function() { this.asdf3=1; this.method(12,12).asdf=1; } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { System.out.println(42); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean args = true; System.out.println(args); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(false); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void function() { boolean b = true; System.out.println(new Class()); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public int m; public void m() { } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class System; public void bla() { System.out.println(); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public void bla() { this.bla(); } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Class { public static void main(String[] args) {} public Class classA; public void bla(Class classB) { { Class classB; } } }");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Main{public static void main(String[] vargs){vargs[5]=null;}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);

		parser = TestUtils
				.initParser("class Main{public static void main(String[] vargs){int a = a;}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);

		parser = TestUtils
				.initParser("class Main{public static void asdf(String[] vargs){return 0;}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);

		parser = TestUtils
				.initParser("class Main{public void asdf; public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Main{public void[] asdf() { return null; } public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Main{public __0_ _0__ (_0I_ _oO0) { return null; } public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);

		parser = TestUtils
				.initParser("class Main{public void asdf() { this.asdf(classA); } public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Main{public int asdf() { return this * 42; } public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);
	}

	@Test
	public void testVoidParams() throws IOException, ParsingFailedException {
		Parser parser = TestUtils
				.initParser("class Main{public void asdf(void a, void b, void c) { return; } public static void main(String[] vargs){}}");
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(3, semanticResult);
	}

	@Test
	public void testSitzung6Errors() throws IOException, ParsingFailedException {
		Parser parser = TestUtils
				.initParser("class Main{public void a() { return b(); } public void b() {} public static void main(String[] vargs){}}");
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(1, semanticResult);

		parser = TestUtils
				.initParser("class Main{public void a(int a, int a, int a) { } public static void main(String[] vargs){}}");
		semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(2, semanticResult);
	}

	@Test
	public void testSystemOutPrintln() throws IOException,
			ParsingFailedException {
		Parser parser = TestUtils
				.initParser("class main{public static void main(String[] a){}} class System { public System System; public System out; public void println(){System.out.println();}}");
		SemanticCheckResults semanticResult = SemanticChecker.checkSemantic(parser.parse());
		assertNumberOfErrors(0, semanticResult);
	}

	private void assertNumberOfErrors(int expectedErrors, SemanticCheckResults semanticResult) {
		assertEquals(expectedErrors, semanticResult.getNumberOfExceptions());
		for (SemanticAnalysisException curr : semanticResult.getExceptions()) {
			assertNotNull(curr);
		}
	}
}
