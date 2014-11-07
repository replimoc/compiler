package compiler.lexer;

public class Position {
	private final int line;
	private final int character;

	public Position(int line, int character) {
		this.line = line;
		this.character = character;
	}

	public int getLine() {
		return line;
	}

	public int getCharacter() {
		return character;
	}

	@Override
	public String toString() {
		return String.format("line %3d, character %2d", line, character);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + character;
		result = prime * result + line;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (character != other.character)
			return false;
		if (line != other.line)
			return false;
		return true;
	}
}
