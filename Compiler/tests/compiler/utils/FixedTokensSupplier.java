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

	public FixedTokensSupplier(Token[] tokens) {
		this.tokens = tokens;
	}

	@Override
	public Token getNextToken() throws IOException {
		Token next = getToken(0);
		index++;
		return next;
	}

	@Override
	public Token getLookAhead() throws IOException {
		return getToken(0);
	}

	@Override
	public Token get2LookAhead() throws IOException {
		return getToken(1);
	}

	private Token getToken(int offset) {
		if (index + offset >= tokens.length) {
			return null;
		} else {
			return tokens[index + offset];
		}
	}

	public void reset() {
		index = 0;
	}

}
