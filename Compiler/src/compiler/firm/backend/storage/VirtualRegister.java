package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class VirtualRegister extends RegisterBased {

	private RegisterBased register;

	private int firstOccurrence = Integer.MAX_VALUE;
	private int lastOccurrence = 0;

	public VirtualRegister() {
		this.register = null;
	}

	public VirtualRegister(RegisterBased register) {
		this.register = register;
	}

	@Override
	public String toString(Bit bit) {
		return this.register.toString(bit);
	}

	public RegisterBased getRegister() {
		return register;
	}

	public void setRegister(RegisterBased register) {
		this.register = register;
	}

	public int getFirstOccurrence() {
		return firstOccurrence;
	}

	public int getLastOccurrence() {
		return lastOccurrence;
	}

	public void setOccurrence(int occurrence) {
		if (occurrence < this.firstOccurrence) {
			this.firstOccurrence = occurrence;
		}
		if (occurrence > this.lastOccurrence) {
			this.lastOccurrence = occurrence;
		}
	}
}
