package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class VirtualRegister extends RegisterBased {

	private static int I = 0;
	private final int num;

	private Storage register;
	private boolean forceRegister;
	private boolean isSpilled;

	private int firstOccurrence = Integer.MAX_VALUE;
	private int lastOccurrence = 0;

	public VirtualRegister() {
		this.register = null;
		this.forceRegister = false;
		this.num = I++;
	}

	public VirtualRegister(RegisterBased register) {
		this.register = register;
		this.forceRegister = true;
		this.num = I++;
	}

	@Override
	public String toString() {
		return "VirtualRegister" + num + "[" + register + "," + firstOccurrence + "," + lastOccurrence + "]";
	}

	@Override
	public String toString(Bit bit) {
		return this.register.toString(bit);
	}

	public Storage getRegister() {
		return register;
	}

	public void setStorage(Storage register) {
		this.register = register;
	}

	public void setForceRegister(boolean forceRegister) {
		this.forceRegister = forceRegister;
	}

	public boolean isForceRegister() {
		return forceRegister;
	}

	public void setSpilled(boolean isSpilled) {
		this.isSpilled = isSpilled;
	}

	@Override
	public boolean isSpilled() {
		return isSpilled;
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
