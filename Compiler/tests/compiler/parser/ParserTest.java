package compiler.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import compiler.ast.Program;
import compiler.lexer.TokenType;
import compiler.utils.TestUtils;

public class ParserTest {

	private Program parseWithEof(TokenType... tokens) throws IOException, ParsingFailedException {
		TokenType[] tokensEof = new TokenType[tokens.length + 1];
		for (int i = 0; i < tokens.length; i++) {
			tokensEof[i] = tokens[i];
		}
		tokensEof[tokens.length] = TokenType.EOF;

		Parser parser = TestUtils.initParser(tokensEof);
		return parser.parse();
	}

	private Program parseWithEofExpectNoErrors(TokenType... tokens) throws IOException, ParsingFailedException {
		try {
			return parseWithEof(tokens);
		} catch (ParsingFailedException ex) {
			ex.printParserExceptions();
			throw ex;
		}
	}

	private void parseWithExpectedErrors(int expectedErrors, TokenType... tokenTypes) throws IOException {
		try {
			parseWithEof(tokenTypes);
			fail();
		} catch (ParsingFailedException e) {
			assertEquals(expectedErrors, e.getDetectedErrors().size());
		}
	}

	@Test
	public void testParseEmptyFile() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors();
	}

	@Test
	public void testParseOnlyIntToken() throws IOException, ParsingFailedException {
		parseWithExpectedErrors(1, TokenType.INT);
	}

	@Test
	public void testParseCurlyBracketClassName() throws IOException, ParsingFailedException {
		parseWithExpectedErrors(2, TokenType.CLASS, TokenType.RCURLYBRACKET, TokenType.LCURLYBRACKET, TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseEmptyClass() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors( // class Class { }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseTwoEmptyClasses() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors( // class ClassA { } class ClassB { }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithField() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors( // class Class { public void field; }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.SEMICOLON,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithEmptyMethod() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors( // class Class { public void method () {} }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.LP, TokenType.RP, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testParseClassWithEmptyMain() throws IOException, ParsingFailedException {
		parseWithEofExpectNoErrors( // class Class { public static void main ( String [] args ) {} }
				TokenType.CLASS, TokenType.IDENTIFIER, TokenType.LCURLYBRACKET,
				TokenType.PUBLIC, TokenType.VOID, TokenType.IDENTIFIER, TokenType.LP, TokenType.IDENTIFIER, TokenType.LSQUAREBRACKET,
				TokenType.RSQUAREBRACKET, TokenType.IDENTIFIER, TokenType.RP, TokenType.LCURLYBRACKET,
				TokenType.RCURLYBRACKET,
				TokenType.RCURLYBRACKET);
	}

	@Test
	public void testFirstProductions() throws IOException, ParsingFailedException {
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

		assertTrue(true);
	}
}
