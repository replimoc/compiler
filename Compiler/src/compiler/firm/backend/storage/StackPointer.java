package compiler.firm.backend.storage;

public class StackPointer extends Storage {

	private final int offset;
	private final Register register;

	public StackPointer(int offset, Register register) {
		this.offset = offset;
		this.register = register;
	}

	@Override
	public String toString() {
		String result;
		if (offset == 0) {
			result = String.format("(%s)", register);
		} else if (offset < 0) {
			result = String.format("-0x%x(%s)", -offset, register);
		} else {
			result = String.format("0x%x(%s)", offset, register);
		}
		return result;
	}

}
