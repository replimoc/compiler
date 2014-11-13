package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.utils.TestUtils;

public class ParseMultipleErrors {
	static String preamble = "class Test {\n\tpublic void test() {\n\t\t";
	static String end = "\n\t}\n}";

	@Test(expected = ParsingFailedException.class)
	public void testWrongFields() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("public int test;public boolean[] main; public []; public void valid;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongClass() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("public int test;public boolean[] main; public []; public void valid;")
				+ createTestString("public int test;public boolean[] main; public []; public void valid;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testOneWrongField() throws IOException, ParsingFailedException {
		System.out.println("class Test {\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		Parser parser = TestUtils
				.initParser("class Test {\n\tpublic int valid;\n\t\tpublic boolean[] valid;\n\t\tpublic [];\n\t\tpublic void valid;\n\t\n}");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongMethods() throws IOException, ParsingFailedException {
		System.out
				.println("class Test {\n\tpublic int test(42++){return;}\n\t\tpublic boolean[] valid;\n\t\tpublic int test(42++){return;}\n\t\tpublic void valid;\n\t\n}");
		Parser parser = TestUtils
				.initParser("class Test {\n\tpublic int test(42++){return;}\n\t\tpublic boolean[] valid;\n\t\tpublic int test(42++){return;}\n\t\tpublic void valid;\n\t\n}");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongWhile() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("while(42)return while(42)return;if(true) return;while(true)42>>=;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongClasses() throws IOException, ParsingFailedException {
		System.out
				.println("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		Parser parser = TestUtils.initParser("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}"
				+ createTestString("public int test;public boolean[] main; public []; public void valid;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongClasses2() throws IOException, ParsingFailedException {
		System.out
				.println("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		System.out
				.println("Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		Parser parser = TestUtils.initParser("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}"
				+ "Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongClasses3() throws IOException, ParsingFailedException {
		System.out
				.println("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		System.out
				.println("Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		System.out
				.println("class Test {\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		Parser parser = TestUtils.initParser("class Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}"
				+ "Test (\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}"
				+ "class Test {\n\tpublic int valid;\n\tpublic boolean[] valid;\n\tpublic [];\n\tpublic void valid;\t\n}");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInClasses() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInClasses2() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test{");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInWhile() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\twhile(42)");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongIf() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("if(true)test++ if(42)return;if(true) return;if(false) 42 else 17;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInIf() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\tif(true)");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongReturn() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("return[]return(42);if(true) return;return test++return;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInReturn() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\treturn");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongExpression() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("44>>=19;17;13;42++;test;"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInExpression() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\t42");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongBlockStatement() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("while(true){42;if(true)return;while(true)return;return[];"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInBlock() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\twhile(true){");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongLocalDeclStatement() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("test test=42++;while(true)return;return[];"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInLDS() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\ttest test=");
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testWrongLocalDeclStatement2() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser(createTestString("test[] invalid=42++;while(true)return;return[];"));
		parser.parse();
	}

	@Test(expected = ParsingFailedException.class)
	public void testEOFInLDS2() throws IOException, ParsingFailedException {
		Parser parser = TestUtils.initParser("class Test {\n\tpublic void test() {\n\t\ttest[] invalid=");
		parser.parse();
	}

	private static String createTestString(String test) {
		String program = preamble + test + end;
		System.out.println(program);
		return program;
	}
}
