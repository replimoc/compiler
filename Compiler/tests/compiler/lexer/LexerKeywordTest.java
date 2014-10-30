package compiler.lexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import compiler.StringTable;

/**
 * Tests for keywords.
 * 
 * @author Aleksej Frank
 *
 */
public class LexerKeywordTest {

	private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }
	
    @Test
	public void testSingleKeywords() {
		String inputStr = "abstract,assert,boolean,break,byte,case,catch,char,class,const,continue,default,double,do,else,enum,extends,false,finally,final,float,for,goto,if,implements,import,instanceof,interface,int,long,native,new,null,package,private,protected,public,return,short,static,strictfp,super,switch,synchronized,this,throws,throw,transient,true,try,void,volatile,while";
		
		List<TokenType> expKeywords = Arrays.asList(new TokenType[] { TokenType.ABSTRACT, 
				TokenType.ASSERT, TokenType.BOOLEAN, TokenType.BREAK, TokenType.BYTE, TokenType.CASE, TokenType.CATCH, TokenType.CHAR, TokenType.CLASS,
				TokenType.CONST, TokenType.CONTINUE, TokenType.DEFAULT, TokenType.DOUBLE, TokenType.DO, TokenType.ELSE, TokenType.ENUM, TokenType.EXTENDS, TokenType.FALSE, 
				TokenType.FINALLY, TokenType.FINAL, TokenType.FLOAT, TokenType.FOR, TokenType.GOTO, TokenType.IF, TokenType.IMPLEMENTS, TokenType.IMPORT, TokenType.INSTANCEOF, TokenType.INTERFACE, 
				TokenType.INT, TokenType.LONG, TokenType.NATIVE, TokenType.NEW, TokenType.NULL, TokenType.PACKAGE, TokenType.PRIVATE, TokenType.PROTECTED, TokenType.PUBLIC, TokenType.RETURN, TokenType.SHORT, 
				TokenType.STATIC, TokenType.STRICTFP, TokenType.SUPER, TokenType.SWITCH, TokenType.SYNCHRONIZED, TokenType.THIS, TokenType.THROWS, TokenType.THROW, TokenType.TRANSIENT, TokenType.TRUE, TokenType.TRY, TokenType.VOID, TokenType.VOLATILE, TokenType.WHILE
		});
		
		String[] keywords = inputStr.split(",");
		
		for (int i = 0; i < keywords.length; i++) {
			BufferedInputStream is = getBufferedInputStream(keywords[i]);
			try {
				Lexer lexer = new Lexer(is, stringTable);
	
				Token token = lexer.getNextToken();
				testToken(token, expKeywords.get(i));
				
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
				TokenType.ASSERT, TokenType.BOOLEAN, TokenType.BREAK, TokenType.BYTE, TokenType.CASE, TokenType.CATCH, TokenType.CHAR, TokenType.CLASS,
				TokenType.CONST, TokenType.CONTINUE, TokenType.DEFAULT, TokenType.DOUBLE, TokenType.DO, TokenType.ELSE, TokenType.ENUM, TokenType.EXTENDS, TokenType.FALSE, 
				TokenType.FINALLY, TokenType.FINAL, TokenType.FLOAT, TokenType.FOR, TokenType.GOTO, TokenType.IF, TokenType.IMPLEMENTS, TokenType.IMPORT, TokenType.INSTANCEOF, TokenType.INTERFACE, 
				TokenType.INT, TokenType.LONG, TokenType.NATIVE, TokenType.NEW, TokenType.NULL, TokenType.PACKAGE, TokenType.PRIVATE, TokenType.PROTECTED, TokenType.PUBLIC, TokenType.RETURN, TokenType.SHORT, 
				TokenType.STATIC, TokenType.STRICTFP, TokenType.SUPER, TokenType.SWITCH, TokenType.SYNCHRONIZED, TokenType.THIS, TokenType.THROWS, TokenType.THROW, TokenType.TRANSIENT, TokenType.TRUE, TokenType.TRY, TokenType.VOID, TokenType.VOLATILE, TokenType.WHILE
		});
		
		
		BufferedInputStream is = getBufferedInputStream(inputStr);
		try {
			Lexer lexer = new Lexer(is, stringTable);

			Token token = null;
			for (int i = 0; i < keywords.size() - 1; i++) {
				token = lexer.getNextToken();
				testToken(token, keywords.get(i));
				
				token = lexer.getNextToken();
				testToken(token, TokenType.COMMA);
			}
			
			token = lexer.getNextToken();
			testToken(token, keywords.get(keywords.size() - 1));
			
			token = lexer.getNextToken();
			Assert.assertEquals(TokenType.EOF, token.getType());

		} catch (IOException e) {
			e.printStackTrace();
			org.junit.Assert.fail();
		}
	}
	
	private void testToken(Token token, TokenType expectedType) {
		Assert.assertEquals(expectedType, token.getType());
	}

	private BufferedInputStream getBufferedInputStream(String empty) {
		return new BufferedInputStream(new ByteArrayInputStream(
				empty.getBytes(StandardCharsets.US_ASCII)));
	}
}
