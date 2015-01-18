package compiler.firm.backend;

public enum Bit {
	// NOTE: DO NOT REORDER THIS ENUM!
	BIT64("q"),
	BIT32("l"),
	BIT8("b");

	private final String suffix;

	Bit(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String toString() {
		return suffix;
	}
}
