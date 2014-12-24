package compiler.firm.backend;

public enum Bit {
	BIT32("l"),
	BIT64("q");

	private final String suffix;

	Bit(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String toString() {
		return suffix;
	}
}
