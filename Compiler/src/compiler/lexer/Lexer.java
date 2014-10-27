package compiler.lexer;

import java.io.BufferedInputStream;
import java.io.IOException;

import compiler.StringTable;

public class Lexer {
	private int c;
	private IReader reader;
	private StringTable stringTable;

	public Lexer(BufferedInputStream bufferedInputStream, StringTable stringTable) throws Exception {
		this.reader = new Reader(bufferedInputStream);
		this.stringTable = stringTable;
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
			t = token(TokenType.INTEGER, "0");
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

	private Token token(TokenType tokenType, String value) {
		return new Token(tokenType, reader.getPosition(), value);
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
		String text = "";
		do {
			text += Character.toChars(c);
			nextChar();
		} while (isAZaz_09());
		return token(TokenType.IDENTIFIER, text);
	}

	private Token lexIntegerLiteral() throws IOException {
		String num = "";
		do {
			num += Character.toChars(c);
			nextChar();
		} while (is09());
		return token(TokenType.INTEGER, num);
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
				// TODO
			} else { // /
				// TODO
			}
			break;
		case '!':
			nextChar();
			if (c == '=') { // !=
				nextChar();
				// TODO
			} else { // !
				// TODO
			}
			break;
		case '%':
			nextChar();
			if (c == '=') { // %=
				nextChar();
				// TODO
			} else { // %
				// TODO
			}
			break;
		case '&':
			nextChar();
			if (c == '&') { // &&
				nextChar();
				// TODO
			} else if (c == '=') { // &=
				nextChar();
				// TODO
			} else { // &
				// TODO
			}
			break;
		case '(':
			nextChar();
			// TODO
			break;
		case ')':
			nextChar();
			// TODO
			break;
		case '*':
			nextChar();
			if (c == '=') { // *=
				nextChar();
				// TODO
			} else { // *
				// TODO
			}
			break;
		case '+':
			nextChar();
			if (c == '+') { // ++
				nextChar();
				// TODO
			} else if (c == '=') { // +=
				nextChar();
				// TODO
			} else { // +
				// TODO
			}
			break;
		case '-':
			nextChar();
			if (c == '-') { // --
				nextChar();
				// TODO
			} else if (c == '=') { // -=
				nextChar();
				// TODO
			} else { // -
				// TODO
			}
			break;
		case '.':
			nextChar();
			// TODO
			break;
		case ',':
			nextChar();
			// TODO
			break;
		case ':':
			nextChar();
			// TODO
			break;
		case ';':
			nextChar();
			// TODO
			break;
		case '=':
			nextChar();
			if (c == '=') { // ==
				nextChar();
				// TODO
			} else { // =
				// TODO
			}
			break;
		case '<':
			nextChar();
			if (c == '<') { // <<
				nextChar();
				if (c == '=') { // <<=
					nextChar();
					// TODO
				} else { // <<
					// TODO
				}
			} else if (c == '=') { // <=
				nextChar();
				// TODO
			} else { // <
				// TODO
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
						// TODO
					} else { // >>>
						// TODO
					}
				} else if (c == '=') { // >>=
					nextChar();
					// TODO
				} else { // >>

				}
			} else if (c == '=') { // >=
				nextChar();
				// TODO
			} else { // >
				// TODO
			}
			break;
		case '?':
			nextChar();
			// TODO
			break;
		case '[':
			nextChar();
			// TODO
			break;
		case ']':
			nextChar();
			// TODO
			break;
		case '^':
			nextChar();
			// TODO
			break;
		case '{':
			nextChar();
			// TODO
			break;
		case '|':
			nextChar();
			if (c == '|') { // ||
				nextChar();
				// TODO
			} else if (c == '=') { // |=
				nextChar();
				// TODO
			} else { // |
				// TODO
			}
			break;
		case '}':
			nextChar();
			// TODO
			break;
		case '~':
			nextChar();
			// TODO
			break;
		default:
			t = token(TokenType.ERROR, "Unexpected char '" + c + "'");
			nextChar();
			break;
		}
		return t;
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

		public Position getPosition() {
			return new Position(line, character);
		}
	}
}
