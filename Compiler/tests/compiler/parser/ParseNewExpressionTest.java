package compiler.parser;

import java.io.IOException;

import org.junit.Test;

import compiler.lexer.TokenType;
import compiler.utils.PrivateMethodCaller;
import compiler.utils.TestUtils;

public class ParseNewExpressionTest {
	private final PrivateMethodCaller caller = new PrivateMethodCaller(Parser.class);;

	@Test
	public void testWithIdent() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.LP, TokenType.RP);
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithArray() throws IOException {
		Parser parser = TestUtils.initParser("test [42]");
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithArray2dim() throws IOException {
		Parser parser = TestUtils.initParser("test [true][]");
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithArrayAssignment() throws IOException {
		Parser parser = TestUtils.initParser("test [null=null][][][]");
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithInt() throws IOException {
		Parser parser = TestUtils.initParser("int [this][]");
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithBool() throws IOException {
		Parser parser = TestUtils.initParser("boolean [fancyexpression=42][]");
		caller.call("parseNewExpression", parser);
	}

	@Test
	public void testWithVoid() throws IOException {
		Parser parser = TestUtils.initParser("void [main][]");
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidExpressionInArray() throws IOException {
		Parser parser = TestUtils.initParser("test [(]");
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidNewObject() throws IOException {
		Parser parser = TestUtils.initParser("test )[]");
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidExpressionInArray3dim() throws IOException {
		Parser parser = TestUtils.initParser("test [false][][)]");
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidExpressionInArray2dim() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.IDENTIFIER, TokenType.LSQUAREBRACKET, TokenType.TRUE, TokenType.RSQUAREBRACKET,
				TokenType.LSQUAREBRACKET, TokenType.INTEGER, TokenType.RSQUAREBRACKET);
		caller.call("parseNewExpression", parser);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidObject() throws IOException {
		Parser parser = TestUtils.initParser(TokenType.BOOLEAN, TokenType.LP, TokenType.RP);
		caller.call("parseNewExpression", parser);
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

}
