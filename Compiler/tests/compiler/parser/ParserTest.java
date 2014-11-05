package compiler.parser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import compiler.lexer.TokenType;
import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParserTest {

	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	private void parseWithEof(TokenType... tokens) throws IOException, ParserException {
		TokenType[] tokensEof = new TokenType[tokens.length + 1];
		for (int i = 0; i < tokens.length; i++) {
			tokensEof[i] = tokens[i];
		}
		tokensEof[tokens.length] = TokenType.EOF;

		Parser parser = TestUtils.initParser(tokensEof);
		parser.parse();
	}

	@Test
	public void testParseEmptyFile() throws IOException, ParserException {
		parseWithEof();
	}

	@Test
	public void testParseEmptyClass() throws IOException, ParserException {
		parseWithEof( // class Class { }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseTwoEmptyClasses() throws IOException, ParserException {
		parseWithEof( // class ClassA { } class ClassB { }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithField() throws IOException, ParserException {
		parseWithEof( // class Class { public void field; }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.SEMICOLON,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithEmptyMethod() throws IOException, ParserException {
		parseWithEof( // class Class { public void method () {} }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.LP, TokenType.RP, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithEmptyMain() throws IOException, ParserException {
		parseWithEof( // class Class { public static void main ( String [] args ) {} }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.LP, TokenType.IDENTIFIER, TokenType.LSQUAREBRACKET,
				TokenType.RSQUAREBRACKET, TokenType.IDENTIFIER, TokenType.RP, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testFirstProductions() throws IOException, ParserException {
		Parser parser;

		parser = TestUtils.initParser("class Class { public void function(int param) {} }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB) {} }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function(int paramA, void paramB, int[] paramC, int[][] paramD) {} }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function() { {} } }");
		parser.parse();

		parser = TestUtils.initParser("class Class { public void function() { ; ; {} } }");
		parser.parse();

		/*
		 * parser = TestUtils.initParser("class Class { public void function() { if () {} } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { while () {} } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { return; } }"); parser.parse(); parser =
		 * TestUtils.initParser("class Class { public void function() { int asdf = ; } }"); parser.parse();
		 */

		assertTrue(true);
	}

	@Test
	public void testParseArrayAccess() throws IOException {
		// TODO: add test cases once Expression is implemented

		/*
		 * try { Parser parser = TestUtils.initParser("[]"); caller.call("parseArrayAccess", parser);
		 * 
		 * } catch (RuntimeException e) { fail(e.getCause().getCause().getMessage()); }
		 */
	}

	@Test
	public void testParseNewExpression() throws IOException {

		// TODO: change test cases once Expression is implemented
		// NewArrayExpression -> [Expression] ([])*

		// correct
		try {
			Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.LP, TokenType.RP);
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("test []");
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("test [][]");
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("test [][][][]");
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("int [][]");
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("boolean [][]");
			caller.call("parseNewExpression", parser);

			parser = TestUtils.initParser("void [][]");
			caller.call("parseNewExpression", parser);

		} catch (RuntimeException e) {
			fail(e.getCause().getCause().getMessage());
		}

		// NewArrayExpression -> [Expression] ([])*
		// errors like "[][" can be caught later

		try {
			Parser parser = TestUtils.initParser("test [(]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("test [[]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("test )[]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("test [][][)]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("test [][][()]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("test [][][42]");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("boolean()");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("void()");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("int()");
			caller.call("parseNewExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidArray() throws IOException {
		Parser parser = TestUtils.initParser( // test ([])
				TokenType.IDENTIFIER, TokenType.LP, TokenType.LSQUAREBRACKET, TokenType.RSQUAREBRACKET, TokenType.RP);
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidTwoDimArray() throws IOException {
		Parser parser = TestUtils.initParser( // test [[]]
				TokenType.IDENTIFIER, TokenType.LSQUAREBRACKET, TokenType.LSQUAREBRACKET, TokenType.RSQUAREBRACKET, TokenType.RSQUAREBRACKET);
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testParsePrimaryExpression() throws IOException {
		// TODO: add test cases once Expression is implemented
		// PrimaryExpression -> null | false | true | INTEGER_LITERAL | PrimaryIdent | this | ( Expression ) | new NewExpression

		// correct
		try {
			Parser parser = TestUtils.initParser("null");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("false");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("true");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("test");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("42");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("0");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("this");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("new test()");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("new int[][]");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("new boolean[][][][]");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("new void[]");
			caller.call("parsePrimaryExpression", parser);

			parser = TestUtils.initParser("new test[]");
			caller.call("parsePrimaryExpression", parser);

		} catch (RuntimeException e) {
			fail(e.getCause().getCause().getMessage());
		}

		// PrimaryExpression -> null | false | true | INTEGER_LITERAL | PrimaryIdent | this | ( Expression ) | new NewExpression

		// not correct
		try {
			Parser parser = TestUtils.initParser("+");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("-");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("/");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new int()");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new boolean()");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new void()");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new +");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new int[[]]");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}

		try {
			Parser parser = TestUtils.initParser("new void[][18]");
			caller.call("parsePrimaryExpression", parser);
			fail("Parser did not catch the error.");
		} catch (RuntimeException e) {
			// error caught
		}
	}
}
