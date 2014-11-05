package compiler.utils;

import java.io.IOException;

import compiler.lexer.Token;
import compiler.lexer.TokenSuppliable;
import compiler.lexer.TokenType;

public class FixedTokensSupplier implements TokenSuppliable {

	private final Token[] tokens;
	private int index = 0;

	public FixedTokensSupplier(TokenType... types) {
		this.tokens = new Token[types.length];
		for (int i = 0; i < types.length; i++) {
			this.tokens[i] = new Token(types[i], null);
		}
	}

	@Override
	public Token getNextToken() throws IOException {
		if (index >= tokens.length) {
			return null;
		}

		Token next = tokens[index];
		index++;
		return next;
	}

	@Override
	public Token getLookAhead() throws IOException {
		if (index + 1 >= tokens.length) {
			return null;
		}

		return tokens[index + 1];
	}

}
