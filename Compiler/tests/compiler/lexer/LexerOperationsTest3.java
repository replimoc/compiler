package compiler.lexer;

import compiler.StringTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test case for errors in operators that should be parser errors, like %==
 *
 * @author effenok
 */
public class LexerOperationsTest3 {

    private StringTable stringTable;

    @Before
    public void setUp() throws Exception {
        stringTable = new StringTable();
    }

    @Test
    public void testSomeOps() throws Exception {
        String[] testStrings = {"%==", "%==***=+0", "a===b-c--d", "a%==/**/--%=",
                    "--a--b-c--++--", "{{[[+]=======-}!?!?!?~~"};
        for (String testString : testStrings) {
            testNoError(testString);
        }
    }

    private void testNoError(String testString) throws Exception {
        Lexer lexer = initLexer(testString);
        Token tok;
        while ((tok = lexer.getNextToken()) != null)
        {
            Assert.assertNotEquals(TokenType.ERROR, tok.getType());
        }
    }

    private Lexer initLexer(String program) throws IOException {
        BufferedInputStream is =
                new BufferedInputStream(new ByteArrayInputStream(program.getBytes(StandardCharsets.US_ASCII)));
        return new Lexer(is, stringTable);
    }
}
