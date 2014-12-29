package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class StackPointer extends Storage {

	private final int offset;
	private final RegisterBased register;

	public StackPointer(int offset, RegisterBased register) {
		this.offset = offset;
		this.register = register;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public String toString(Bit bit) {
		// Always use 64 bit register, this are stack addresses.
		String result;
		if (offset == 0) {
			result = String.format("(%s)", register.toString(Bit.BIT64));
		} else if (offset < 0) {
			result = String.format("-0x%x(%s)", -offset, register.toString(Bit.BIT64));
		} else {
			result = String.format("0x%x(%s)", offset, register.toString(Bit.BIT64));
		}
		return result;
	}

	public int getOffset() {
		return offset;
	}

	@Override
	public RegisterBased getUsedRegister() {
		return register;
	}
}
