package compiler.lexer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compiler.utils.TestUtils;

/**
 * Tests for keywords.
 * 
 * @author Aleksej Frank
 *
 */
public class LexerKeywordTest {

	@Test
	public void testSingleKeywords() {
		String inputStr = "abstract,assert,boolean,break,byte,case,catch,char,class,const,continue,default,double,do,else,enum,extends,false,finally,final,float,for,goto,if,implements,import,instanceof,interface,int,long,native,new,null,package,private,protected,public,return,short,static,strictfp,super,switch,synchronized,this,throws,throw,transient,true,try,void,volatile,while";

		List<TokenType> expKeywords = Arrays.asList(new TokenType[] { TokenType.ABSTRACT,
				TokenType.ASSERT, TokenType.BOOLEAN, TokenType.BREAK, TokenType.BYTE, TokenType.CASE, TokenType.CATCH, TokenType.CHAR,
				TokenType.CLASS,
				TokenType.CONST, TokenType.CONTINUE, TokenType.DEFAULT, TokenType.DOUBLE, TokenType.DO, TokenType.ELSE, TokenType.ENUM,
				TokenType.EXTENDS, TokenType.FALSE,
				TokenType.FINALLY, TokenType.FINAL, TokenType.FLOAT, TokenType.FOR, TokenType.GOTO, TokenType.IF, TokenType.IMPLEMENTS,
				TokenType.IMPORT, TokenType.INSTANCEOF, TokenType.INTERFACE,
				TokenType.INT, TokenType.LONG, TokenType.NATIVE, TokenType.NEW, TokenType.NULL, TokenType.PACKAGE, TokenType.PRIVATE,
				TokenType.PROTECTED, TokenType.PUBLIC, TokenType.RETURN, TokenType.SHORT,
				TokenType.STATIC, TokenType.STRICTFP, TokenType.SUPER, TokenType.SWITCH, TokenType.SYNCHRONIZED, TokenType.THIS, TokenType.THROWS,
				TokenType.THROW, TokenType.TRANSIENT, TokenType.TRUE, TokenType.TRY, TokenType.VOID, TokenType.VOLATILE, TokenType.WHILE
		});

		String[] keywords = inputStr.split(",");

		for (int i = 0; i < keywords.length; i++) {
			try {
				Lexer lexer = TestUtils.initLexer(keywords[i]);

				Token token = lexer.getNextToken();
				assertTokenType(expKeywords.get(i), token);

				token = lexer.getNextToken();
				Assert.assertEquals(TokenType.EOF, token.getType());

			} catch (IOException e) {
				e.printStackTrace();
				org.junit.Assert.fail();
			}
		}
	}

	@Test
	public void testConcatKeywords() {
		String inputStr = "abstract,assert,boolean,break,byte,case,catch,char,class,const,continue,default,double,do,else,enum,extends,false,finally,final,float,for,goto,if,implements,import,instanceof,interface,int,long,native,new,null,package,private,protected,public,return,short,static,strictfp,super,switch,synchronized,this,throws,throw,transient,true,try,void,volatile,while";

		List<TokenType> keywords = Arrays.asList(new TokenType[] { TokenType.ABSTRACT,
				TokenType.ASSERT, TokenType.BOOLEAN, TokenType.BREAK, TokenType.BYTE, TokenType.CASE, TokenType.CATCH, TokenType.CHAR,
				TokenType.CLASS,
				TokenType.CONST, TokenType.CONTINUE, TokenType.DEFAULT, TokenType.DOUBLE, TokenType.DO, TokenType.ELSE, TokenType.ENUM,
				TokenType.EXTENDS, TokenType.FALSE,
				TokenType.FINALLY, TokenType.FINAL, TokenType.FLOAT, TokenType.FOR, TokenType.GOTO, TokenType.IF, TokenType.IMPLEMENTS,
				TokenType.IMPORT, TokenType.INSTANCEOF, TokenType.INTERFACE,
				TokenType.INT, TokenType.LONG, TokenType.NATIVE, TokenType.NEW, TokenType.NULL, TokenType.PACKAGE, TokenType.PRIVATE,
				TokenType.PROTECTED, TokenType.PUBLIC, TokenType.RETURN, TokenType.SHORT,
				TokenType.STATIC, TokenType.STRICTFP, TokenType.SUPER, TokenType.SWITCH, TokenType.SYNCHRONIZED, TokenType.THIS, TokenType.THROWS,
				TokenType.THROW, TokenType.TRANSIENT, TokenType.TRUE, TokenType.TRY, TokenType.VOID, TokenType.VOLATILE, TokenType.WHILE
		});

		try {
			Lexer lexer = TestUtils.initLexer(inputStr);

			Token token = null;
			for (int i = 0; i < keywords.size() - 1; i++) {
				token = lexer.getNextToken();
				assertTokenType(keywords.get(i), token);

				token = lexer.getNextToken();
				assertTokenType(TokenType.COMMA, token);
			}

			token = lexer.getNextToken();
			assertTokenType(keywords.get(keywords.size() - 1), token);

			token = lexer.getNextToken();
			Assert.assertEquals(TokenType.EOF, token.getType());

		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
	}

	private void assertTokenType(TokenType expectedType, Token token) {
		Assert.assertEquals(expectedType, token.getType());
	}
}
