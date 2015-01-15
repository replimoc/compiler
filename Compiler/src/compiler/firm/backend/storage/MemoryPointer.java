package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class MemoryPointer extends Storage {

	private int offset;
	private RegisterBased register;
	private RegisterBased factorRegister;
	private int factor;

	public MemoryPointer(int offset, RegisterBased register) {
		this(offset, register, null, 0);
	}

	public MemoryPointer(int offset, RegisterBased register, RegisterBased factorRegister, int factor) {
		this.offset = offset;
		this.register = register;
		this.factorRegister = factorRegister;
		this.factor = factor;
	}

	@Override
	public String toString() {
		// Always use 64 bit register, this are stack addresses.
		String secondRegister = "";
		if (factorRegister != null) {
			secondRegister = String.format(",%s,%d", factorRegister.toString(), factor);
		}

		String result;
		if (offset == 0) {
			result = String.format("(%s%s)", register.toString(), secondRegister);
		} else if (offset < 0) {
			result = String.format("-0x%x(%s%s)", -offset, register.toString(), secondRegister);
		} else {
			result = String.format("0x%x(%s%s)", offset, register.toString(), secondRegister);
		}
		return result;
	}

	public int getOffset() {
		return offset;
	}

	@Override
	public RegisterBased[] getReadOnRightSideRegister() {
		return getUsedRegister();
	}

	@Override
	public RegisterBased[] getUsedRegister() {
		if (factorRegister == null) {
			return new RegisterBased[] { register };
		}
		return new RegisterBased[] { register, factorRegister };
	}

	@Override
	public boolean isSpilled() {
		return true;
	}

	@Override
	public Bit getMode() {
		return null; // TODO implement this correctly
	}
}