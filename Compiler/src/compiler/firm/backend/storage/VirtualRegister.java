package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public class VirtualRegister extends RegisterBased {

	private Register register;

	public VirtualRegister() {
		this.register = null;
	}

	public VirtualRegister(Register forceRegister) {
		this.register = forceRegister;
	}

	@Override
	public String toString(Bit bit) {
		return this.register.toString(bit);
	}
}
