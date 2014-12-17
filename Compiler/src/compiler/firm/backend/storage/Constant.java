package compiler.firm.backend.storage;

public class Constant extends Storage {
	private final int constant;

	public Constant(int constant) {
		this.constant = constant;
	}

	@Override
	public String toString() {
		String result;
		if (constant < 0) {
			result = String.format("$-0x%x", -constant);
		} else {
			result = String.format("$0x%x", constant);
		}
		return result;
	}
}
