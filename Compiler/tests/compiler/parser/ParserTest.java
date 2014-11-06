package compiler.parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import compiler.lexer.TokenType;
import compiler.utils.TestUtils;

public class ParserTest {

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
}
