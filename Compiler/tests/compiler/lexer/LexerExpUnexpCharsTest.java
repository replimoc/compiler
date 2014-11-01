package compiler.lexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import compiler.StringTable;

public class LexerExpUnexpCharsTest {

	private StringTable stringTable;

	@Before
	public void setUp() throws Exception {
		stringTable = new StringTable();
	}

	@Test
	public void test0till32Chars() {
		for (int i = 0; i <= 32; i++) {
			String inputStr = Character.toString((char) i);
			BufferedInputStream is = getBufferedInputStream(inputStr);

			try {
				Lexer lexer = new Lexer(is, stringTable);

				// allowed empty chars
				if (i == 9 || i == 10 || i == 13 || i == 32) {
					Token tok = lexer.getNextToken();

					Assert.assertEquals(TokenType.EOF, tok.getType());

					tok = lexer.getNextToken();
					Assert.assertEquals(null, tok);
				} else {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.ERROR, tok.getType());

					tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.EOF, tok.getType());

					tok = lexer.getNextToken();
					Assert.assertEquals(null, tok);
				}
			} catch (IOException e) {
				e.printStackTrace();
				org.junit.Assert.fail();
			}
		}
	}

	@Test
	public void test33till127Chars() {
		for (int i = 33; i <= 127; i++) {
			String inputStr = Character.toString((char) i);
			BufferedInputStream is = getBufferedInputStream(inputStr);
			try {
				Lexer lexer = new Lexer(is, stringTable);

				// allowed chars
				if (i == 33) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LOGICALNOT, tok.getType());
				} else if (i == 37) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.MODULO, tok.getType());
				} else if (i == 38) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.AND, tok.getType());
				} else if (i == 40) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LP, tok.getType());
				} else if (i == 40) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LP, tok.getType());
				} else if (i == 41) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.RP, tok.getType());
				} else if (i == 42) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.MULTIPLY, tok.getType());
				} else if (i == 43) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.ADD, tok.getType());
				} else if (i == 44) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.COMMA, tok.getType());
				} else if (i == 45) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.SUBTRACT, tok.getType());
				} else if (i == 46) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.POINT, tok.getType());
				} else if (i == 47) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.DIVIDE, tok.getType());
				} else if (i >= 48 && i <= 57) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.INTEGER, tok.getType());
					Assert.assertEquals(String.valueOf(i - 48), tok.getSymbol()
							.getValue());
				} else if (i == 58) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.COLON, tok.getType());
				} else if (i == 59) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.SEMICOLON, tok.getType());
				} else if (i == 60) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LESS, tok.getType());
				} else if (i == 61) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.ASSIGN, tok.getType());
				} else if (i == 62) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.GREATER, tok.getType());
				} else if (i == 63) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.CONDITIONAL, tok.getType());
				} else if (i >= 65 && i <= 90) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.IDENTIFIER, tok.getType());
					Assert.assertEquals(Character.toString((char) i), tok
							.getSymbol().getValue());
				} else if (i == 91) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LSQUAREBRACKET, tok.getType());
				} else if (i == 91) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LSQUAREBRACKET, tok.getType());
				} else if (i == 93) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.RSQUAREBRACKET, tok.getType());
				} else if (i == 94) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.EXCLUSIVEOR, tok.getType());
				} else if (i == 95) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.IDENTIFIER, tok.getType());
					Assert.assertEquals("_", tok.getSymbol().getValue());
				} else if (i >= 97 && i <= 122) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.IDENTIFIER, tok.getType());
					Assert.assertEquals(Character.toString((char) i), tok
							.getSymbol().getValue());
				} else if (i == 123) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.LCURLYBRACKET, tok.getType());
				} else if (i == 124) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.INCLUSIVEOR, tok.getType());
				} else if (i == 125) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.RCURLYBRACKET, tok.getType());
				} else if (i == 126) {
					Token tok = lexer.getNextToken();
					Assert.assertEquals(TokenType.BINARYCOMPLEMENT,
							tok.getType());
				// not allowed
				} else {
					Token tok = lexer.getNextToken();

					Assert.assertEquals(TokenType.ERROR, tok.getType());

					tok = lexer.getNextToken();

					Assert.assertEquals(TokenType.EOF, tok.getType());

					tok = lexer.getNextToken();

					Assert.assertEquals(null, tok);
				}
			} catch (IOException e) {
				e.printStackTrace();
				org.junit.Assert.fail();
			}
		}
	}
	
	@Test
	public void testUnexpCharAfterEmptyChars() {
		try {
			String inputStr = "   \n $";
			List<Token> tokens = lexString(inputStr);
			Assert.assertEquals(1, tokens.size());
			Assert.assertEquals(TokenType.ERROR, tokens.get(0).getType());
		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
	}

	@Test
	public void testUnexpCharAfterExpChars() {
		try {
			String inputStr = "abstract$";
			List<Token> tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.ABSTRACT);

			inputStr = "import$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.IMPORT);
			
			inputStr = "!$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.LOGICALNOT);
			
			inputStr = "!=  $";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.NOTEQUAL);
			
			inputStr = "!= \n $";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.NOTEQUAL);
			
			inputStr = "_$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.IDENTIFIER);
			
			inputStr = "_abc$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.IDENTIFIER);
			
			inputStr = "abc_a3df$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.IDENTIFIER);
			
			inputStr = "abc3a3df$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.IDENTIFIER);
			
			inputStr = "42$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.INTEGER);
			
			inputStr = "0$";
			tokens = lexString(inputStr);
			testUnexpCharAfterExpToken(tokens, TokenType.INTEGER);
		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
	}

	private List<Token> lexString(String input) throws IOException {
		BufferedInputStream is = getBufferedInputStream(input);
		Lexer lexer = new Lexer(is, stringTable);

		List<Token> result = new ArrayList<Token>();
		Token tok = null;
		while(true) {
			tok = lexer.getNextToken();
			if (tok.getType() == TokenType.EOF) {
				break;
			}
			result.add(tok);
		}
		return result;
	}
	
	private void testUnexpCharAfterExpToken(List<Token> tokens, TokenType expTok) {
		Assert.assertEquals(2, tokens.size());
		Assert.assertEquals(expTok, tokens.get(0).getType());
		Assert.assertEquals(TokenType.ERROR, tokens.get(1).getType());
	}

	private BufferedInputStream getBufferedInputStream(String empty) {
		return new BufferedInputStream(new ByteArrayInputStream(
				empty.getBytes(StandardCharsets.US_ASCII)));
	}
}
