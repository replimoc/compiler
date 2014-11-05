package compiler.lexer;

import java.io.IOException;

/**
 * Interface defining a supplier of tokens.
 * 
 * @author Andreas Eberle
 *
 */
public interface TokenSuppliable {
	Token getNextToken() throws IOException;

	Token getLookAhead() throws IOException;
}
