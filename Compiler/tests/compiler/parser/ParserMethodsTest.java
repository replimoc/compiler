package compiler.parser;

import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * 
 */
public class ParserMethodsTest {

	static String preamble1 = "class Test {\n\tpublic void test() {\n\t\t";
	static String end1 = "\n\t}\n}";

	static String preamble2 = "class Test {\n";
	static String end2 = "\n}";

	@Test
	public void testReturn() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestStringwMethod("return;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return 1;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return true;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return false;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return iden;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return new iden();"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return new iden[1];"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return new iden[1][][];"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return this.getX(new x()[x]);"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return a + b && c - d || dd.x - -dd.y * dd.z[x];"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("return (a + b && c) - (d || dd.x) - (((-dd.y)) * dd.z[x]);"));
		parser.parse();
	}

	@Test
	public void testWhile() throws Exception {
		Parser parser;

		parser = TestUtils.initParser(createTestStringwMethod("while(1) {};"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("while(1);"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("while(1) if(x) {};"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("while(1) a + b;"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("while(1) while(2) while (3) { while (4) while (5) ;};"));
		parser.parse();
		parser = TestUtils.initParser(createTestStringwMethod("while(1) return 0;"));
		parser.parse();

	}

	private static String createTestStringwMethod(String test) {
		String program = preamble1 + test + end1;
		System.out.println(program);
		return program;
	}
}
