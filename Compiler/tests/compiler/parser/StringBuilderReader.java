package compiler.parser;

import java.io.IOException;
import java.io.Reader;

public class StringBuilderReader extends Reader {

	private final StringBuilder stringBuilder;
	private final int length;
	private int position;

	public StringBuilderReader(StringBuilder stringBuilder) {
		this.stringBuilder = stringBuilder;
		this.length = stringBuilder.length();
	}

	@Override
	public int read(char[] cbuf, int offset, int length) throws IOException {
		length = Math.min(length, this.length - position);

		if (length > 0) {
			stringBuilder.getChars(position, position + length, cbuf, offset);
			position += length;
			return length;
		} else {
			return -1;
		}
	}

	@Override
	public void close() throws IOException {
	}
}
