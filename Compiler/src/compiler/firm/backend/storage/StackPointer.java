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
		// Always use 64 bit register, this are stack addresses.
		String result;
		if (offset == 0) {
			result = String.format("(%s)", register.toString64());
		} else if (offset < 0) {
			result = String.format("-0x%x(%s)", -offset, register.toString64());
		} else {
			result = String.format("0x%x(%s)", offset, register.toString64());
		}
		return result;
	}

	@Override
	public String toString32() {
		return toString();
	}

	@Override
	public String toString64() {
		return toString();
	}

	public int getOffset() {
		return offset;
	}
}
