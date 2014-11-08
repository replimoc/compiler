package compiler.parser;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.TestUtils;

public class ParseMultipleErrors {
	static String preamble = "class Test {\n\tpublic void test() {\n\t\t";
	static String end = "\n\t}\n}";

	@Test
	public void testWrongFields() throws Exception {
		Parser parser = TestUtils.initParser(createTestString("public int test;public boolean[] main; public []; public void valid;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongClass() throws Exception {
		Parser parser = TestUtils.initParser(createTestString("public int test;public boolean[] main; public []; public void valid;")
				+ createTestString("public int test;public boolean[] main; public []; public void valid;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testOneWrongField() throws IOException {
		System.out.println("class Test {\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		Parser parser = TestUtils
				.initParser("class Test {\n\tpublic int valid;\n\t\tpublic boolean[] valid;\n\t\tpublic [];\n\t\tpublic void valid;\n\t\n}");
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongMethods() throws Exception {
		System.out
				.println("class Test {\n\tpublic int test(42++){return;}\n\t\tpublic boolean[] valid;\n\t\tpublic int test(42++){return;}\n\t\tpublic void valid;\n\t\n}");
		Parser parser = TestUtils
				.initParser("class Test {\n\tpublic int test(42++){return;}\n\t\tpublic boolean[] valid;\n\t\tpublic int test(42++){return;}\n\t\tpublic void valid;\n\t\n}");
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongWhile() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("while(42)return while(42)return;if(true) return;while(true)42>>=;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongIf() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("if(true)test++ if(42)return;if(true) return;if(false) 42 else 17;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongReturn() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("return[]return(42);if(true) return;return test++return;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongExpression() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("44>>=19;17;13;42++;test;"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongBlockStatement() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("while(true){42;if(true)return;while(true)return;return[];"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongLocalDeclStatement() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("test test=42++;while(true)return;return[];"));
		assertFalse(parser.parse() == 0);
	}

	@Test
	public void testWrongLocalDeclStatement2() throws IOException {
		Parser parser = TestUtils.initParser(createTestString("test[] invalid=42++;while(true)return;return[];"));
		assertFalse(parser.parse() == 0);
	}

	private static String createTestString(String test) {

		String program = preamble + test + end;
		System.out.println(program);
		return program;
	}
}
