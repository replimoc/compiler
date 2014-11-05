package compiler.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compiler.lexer.TokenType;
import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class PrecedenceClimbingTest {
	private PrivateMethodCaller privateMethodCaller;

	@Before
	public void setUp() throws Exception {
		privateMethodCaller = new PrivateMethodCaller(Parser.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPlus() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.ADD, TokenType.IDENTIFIER, TokenType.EOF);
		assertEquals("(_+_)", callParseExpression(parser));
	}

	@Test
	public void testPlusLeftAssociative() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.ADD, TokenType.IDENTIFIER, TokenType.ADD, TokenType.IDENTIFIER,
				TokenType.EOF);
		assertEquals("((_+_)+_)", callParseExpression(parser));
	}

	@Test
	public void testAllLeftAssociative() throws IOException {
		TokenType[] types = new TokenType[] { TokenType.GREATEREQUAL,
				TokenType.NOTEQUAL,
				TokenType.GREATER,
				TokenType.LESS,
				TokenType.MULTIPLY,
				TokenType.SUBTRACT,
				TokenType.LOGICALAND,
				TokenType.ADD,
				TokenType.LESSEQUAL,
				TokenType.EQUAL,
				TokenType.LOGICALOR,
				TokenType.MODULO,
				TokenType.DIVIDE };

		for (TokenType type : types) {
			Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, type, TokenType.IDENTIFIER, type, TokenType.IDENTIFIER,
					TokenType.EOF);
			assertEquals("((_" + type.getString() + "_)" + type.getString() + "_)", callParseExpression(parser));
		}
	}

	@Test
	public void testAssign() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER, TokenType.EOF);
		assertEquals("(_=_)", callParseExpression(parser));
	}

	@Test
	public void testAssignRightAssociative() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.IDENTIFIER,
				TokenType.EOF);
		assertEquals("(_=(_=_))", callParseExpression(parser));
	}

	@Test
	public void testTokenPrecedence() throws IOException {
		Parser parser1 = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.NOTEQUAL,
				TokenType.IDENTIFIER, TokenType.ASSIGN,
				TokenType.IDENTIFIER, TokenType.MODULO,
				TokenType.IDENTIFIER, TokenType.GREATER,
				TokenType.IDENTIFIER, TokenType.LESS,
				TokenType.IDENTIFIER, TokenType.SUBTRACT,
				TokenType.IDENTIFIER, TokenType.DIVIDE,
				TokenType.IDENTIFIER, TokenType.MULTIPLY,
				TokenType.IDENTIFIER, TokenType.LOGICALAND,
				TokenType.IDENTIFIER, TokenType.LOGICALOR,
				TokenType.IDENTIFIER, TokenType.LESSEQUAL,
				TokenType.IDENTIFIER, TokenType.GREATEREQUAL,
				TokenType.IDENTIFIER, TokenType.EQUAL,
				TokenType.IDENTIFIER, TokenType.ADD,
				TokenType.IDENTIFIER, TokenType.EOF);
		assertEquals("((_!=_)=(((((_%_)>_)<(_-((_/_)*_)))&&_)||(((_<=_)>=_)==(_+_))))", callParseExpression(parser1));

		Parser parser2 = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.ADD,
				TokenType.IDENTIFIER, TokenType.EQUAL,
				TokenType.IDENTIFIER, TokenType.MODULO,
				TokenType.IDENTIFIER, TokenType.LESS,
				TokenType.IDENTIFIER, TokenType.LOGICALOR,
				TokenType.IDENTIFIER, TokenType.ASSIGN,
				TokenType.IDENTIFIER, TokenType.GREATER,
				TokenType.IDENTIFIER, TokenType.GREATEREQUAL,
				TokenType.IDENTIFIER, TokenType.LOGICALAND,
				TokenType.IDENTIFIER, TokenType.DIVIDE,
				TokenType.IDENTIFIER, TokenType.MULTIPLY,
				TokenType.IDENTIFIER, TokenType.NOTEQUAL,
				TokenType.IDENTIFIER, TokenType.LESSEQUAL,
				TokenType.IDENTIFIER, TokenType.SUBTRACT,
				TokenType.IDENTIFIER, TokenType.EOF);
		assertEquals("((((_+_)==((_%_)<_))||_)=(((_>_)>=_)&&(((_/_)*_)!=(_<=(_-_)))))", callParseExpression(parser2));
	}

	private String callParseExpression(Parser parser) {
		return privateMethodCaller.call("parseExpression", parser, new Class<?>[] { int.class }, new Object[] { 0 });
	}

}
