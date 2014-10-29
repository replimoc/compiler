package compiler.lexer;

import java.io.BufferedInputStream;
import java.io.IOException;

import compiler.StringTable;
import compiler.StringTable.Entry;
import compiler.Symbol;

public class Lexer {
	private int c;
	private IReader reader;
	private StringTable stringTable;

	public Lexer(BufferedInputStream bufferedInputStream, StringTable stringTable) throws IOException {
		this.reader = new Reader(bufferedInputStream);
		this.stringTable = stringTable;
		initStringTable(stringTable);
		nextChar();
	}

	public Token getNextToken() throws IOException {
		Token t = null;

		while (isWhitespace()) {
			nextChar();
		}

		if (c == -1) { // Character is EOF
			t = token(TokenType.EOF);
		} else if (isAZaz_()) {
			t = lexIdentifier();
		} else if (is19()) {
			t = lexIntegerLiteral();
		} else if (c == '0') {
			nextChar();
			if (is09()) {
				t = tokenError("Unexpected number after an '0'.");
				while (is09()) { // Parse all integer until another token available.
					nextChar();
				}
			} else {
				t = tokenStringTable(TokenType.INTEGER, "0");
			}
		} else {
			t = lexOperatorAndComment();
		}
		return t;
	}

	private void nextChar() throws IOException {
		c = reader.getChar();
	}

	private Token token(TokenType tokenType) {
		return token(tokenType, null);
	}

	private Token token(TokenType tokenType, Symbol value) {
		return new Token(tokenType, reader.getPosition(), value);
	}

	private Token tokenStringTable(TokenType tokenType, String value) {
		Entry tokenEntry = this.stringTable.insert(value, tokenType);
		return token(tokenEntry.getType(), tokenEntry.getSymbol());
	}

	private Token tokenError(String message) {
		return token(TokenType.ERROR, new Symbol(message));
	}

	/*
	 * Test Functions
	 */
	private boolean isWhitespace() {
		return (c == ' ' ||
				c == '\t' ||
				c == '\n' || c == '\r');
	}

	private boolean isAZaz_() {
		return ((c >= 'A' && c <= 'Z') ||
				(c >= 'a' && c <= 'z') || c == '_');
	}

	private boolean isAZaz_09() {
		return isAZaz_() || is09();
	}

	private boolean is09() {
		return (c >= '0' && c <= '9');
	}

	private boolean is19() {
		return (c >= '1' && c <= '9');
	}

	/*
	 * Lex functions
	 */
	private Token lexIdentifier() throws IOException {
		StringBuffer text = new StringBuffer();
		do {
			text.append((char) c);
			nextChar();
		} while (isAZaz_09());
		return tokenStringTable(TokenType.IDENTIFIER, text.toString());
	}

	private Token lexIntegerLiteral() throws IOException {
		StringBuffer num = new StringBuffer();
		do {
			num.append((char) c);
			nextChar();
		} while (is09());
		return tokenStringTable(TokenType.INTEGER, num.toString());
	}

	private void lexComment() throws IOException {
		do {
			do {
				nextChar();
			} while (c != '*');
			nextChar();
		} while (c != '/');
		nextChar();
	}

	private Token lexOperatorAndComment() throws IOException {
		Token t = null;
		switch (c) {
		case '/': // tokens: /* ... */, /, /=
			nextChar();
			if (c == '*') {
				lexComment();
				t = getNextToken();
			} else if (c == '=') { // /=
				nextChar();
				t = token(TokenType.DIVIDEASSIGN);
			} else { // /
				t = token(TokenType.DIVIDE);
			}
			break;
		case '!':
			nextChar();
			if (c == '=') { // !=
				nextChar();
				t = token(TokenType.NOTEQUAL);
			} else { // !
				t = token(TokenType.LOGICALNOT);
			}
			break;
		case '%':
			nextChar();
			if (c == '=') { // %=
				nextChar();
				t = token(TokenType.MODULOASSIGN);
			} else { // %
				t = token(TokenType.MODULO);
			}
			break;
		case '&':
			nextChar();
			if (c == '&') { // &&
				nextChar();
				t = token(TokenType.LOGICALAND);
			} else if (c == '=') { // &=
				nextChar();
				t = token(TokenType.ANDASSIGN);
			} else { // &
				t = token(TokenType.AND);
			}
			break;
		case '(':
			nextChar();
			t = token(TokenType.LP);
			break;
		case ')':
			nextChar();
			t = token(TokenType.RP);
			break;
		case '*':
			nextChar();
			if (c == '=') { // *=
				nextChar();
				t = token(TokenType.MULTIPLYASSIGN);
			} else { // *
				t = token(TokenType.MULTIPLY);
			}
			break;
		case '+':
			nextChar();
			if (c == '+') { // ++
				nextChar();
				t = token(TokenType.INCREMENT);
			} else if (c == '=') { // +=
				nextChar();
				t = token(TokenType.ADDASSIGN);
			} else { // +
				t = token(TokenType.ADD);
			}
			break;
		case '-':
			nextChar();
			if (c == '-') { // --
				nextChar();
				t = token(TokenType.DECREMENT);
			} else if (c == '=') { // -=
				nextChar();
				t = token(TokenType.SUBTRACTASSIGN);
			} else { // -
				t = token(TokenType.SUBTRACT);
			}
			break;
		case '.':
			nextChar();
			t = token(TokenType.POINT);
			break;
		case ',':
			nextChar();
			t = token(TokenType.COMMA);
			break;
		case ':':
			nextChar();
			t = token(TokenType.COLON);
			break;
		case ';':
			nextChar();
			t = token(TokenType.SEMICOLON);
			break;
		case '=':
			nextChar();
			if (c == '=') { // ==
				nextChar();
				t = token(TokenType.EQUAL);
			} else { // =
				t = token(TokenType.ASSIGN);
			}
			break;
		case '<':
			nextChar();
			if (c == '<') { // <<
				nextChar();
				if (c == '=') { // <<=
					nextChar();
					t = token(TokenType.LSASSIGN);
				} else { // <<
					t = token(TokenType.LS);
				}
			} else if (c == '=') { // <=
				nextChar();
				t = token(TokenType.LESSEQUAL);
			} else { // <
				t = token(TokenType.LESS);
			}
			break;
		case '>':
			nextChar();
			if (c == '>') { // >>
				nextChar();
				if (c == '>') { // >>>
					nextChar();
					if (c == '=') { // >>>=
						nextChar();
						t = token(TokenType.RSZEROFILLASSIGN);
					} else { // >>>
						t = token(TokenType.RSZEROFILL);
					}
				} else if (c == '=') { // >>=
					nextChar();
					t = token(TokenType.RSASSIGN);
				} else { // >>
					t = token(TokenType.RS);
				}
			} else if (c == '=') { // >=
				nextChar();
				t = token(TokenType.GREATEREQUAL);
			} else { // >
				t = token(TokenType.GREATER);
			}
			break;
		case '?':
			nextChar();
			t = token(TokenType.CONDITIONAL);
			break;
		case '[':
			nextChar();
			t = token(TokenType.LSQUAREBRACKET);
			break;
		case ']':
			nextChar();
			t = token(TokenType.RSQUAREBRACKET);
			break;
		case '^':
			nextChar();
			if (c == '=') { // ^=
				nextChar();
				t = token(TokenType.EXCLUSIVEORASSIGN);
			} else { // ^
				t = token(TokenType.EXCLUSIVEOR);
			}
			break;
		case '{':
			nextChar();
			t = token(TokenType.LCURLYBRACKET);
			break;
		case '|':
			nextChar();
			if (c == '|') { // ||
				nextChar();
				t = token(TokenType.LOGICALOR);
			} else if (c == '=') { // |=
				nextChar();
				t = token(TokenType.INCLUSIVEORASSIGN);
			} else { // |
				t = token(TokenType.INCLUSIVEOR);
			}
			break;
		case '}':
			nextChar();
			t = token(TokenType.RCURLYBRACKET);
			break;
		case '~':
			nextChar();
			t = token(TokenType.BINARYCOMPLEMENT);
			break;
		default:
			t = tokenError("Unexpected char '" + c + "'");
			nextChar();
			break;
		}
		return t;
	}

	private static void initStringTable(StringTable stringTable) {
		for (TokenType curr : TokenType.values()) {
			if (curr.isKeyword()) {
				stringTable.insert(curr.getString(), curr);
			}
		}
	}

	/*
	 * Reader for reading buffered input stream.
	 */
	private interface IReader {
		public int getChar() throws IOException;

		public Position getPosition();
	}

	private class Reader implements IReader {
		private BufferedInputStream bufferedInputStream;
		private int line = 1;
		private int character = 0;

		public Reader(BufferedInputStream bufferedInputStream) {
			this.bufferedInputStream = bufferedInputStream;
		}

		@Override
		public int getChar() throws IOException {
			c = bufferedInputStream.read();
			if (c == '\n' || c == '\r') {
				line++;
				character = 0;
			} else {
				character++;
			}
			return c;
		}

		@Override
		public Position getPosition() {
			return new Position(line, character);
		}
	}
}