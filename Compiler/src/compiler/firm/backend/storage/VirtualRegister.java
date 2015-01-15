package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class VirtualRegister extends RegisterBased {

	private static int I = 0;

	private final Bit mode;
	private final int num;

	private Storage register;
	private boolean forceRegister;
	private boolean isSpilled;

	private int firstOccurrence = Integer.MAX_VALUE;
	private int lastOccurrence = 0;

	public VirtualRegister(Bit mode) {
		this(mode, (RegisterBased) null);
	}

	public VirtualRegister(Bit mode, RegisterBased register) {
		this.mode = mode;
		this.register = register;
		this.forceRegister = register != null;
		this.num = I++;
	}

	public VirtualRegister(Bit mode, RegisterBundle registerBundle) {
		this(mode, registerBundle.getRegister(mode));
	}

	@Override
	public String toString() {
		return register == null ? "VR_" + getNum() : register.toString();
		// return "VR_" + getNum();
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

	@Override
	public Bit getMode() {
		return mode;
	}

	public boolean isAliveAt(int line) {
		return firstOccurrence <= line && line <= lastOccurrence;
	}

	public int getNum() {
		return num;
	}

	@Override
	public SingleRegister getSingleRegister() {
		return register.getSingleRegister();
	}

	@Override
	public RegisterBundle getRegisterBundle() {
		return register.getRegisterBundle();
	}
}
