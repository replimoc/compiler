package compiler.firm.backend.storage;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.utils.Utils;

public class MemoryPointer extends Storage {

	protected int offset;
	private RegisterBased register;
	private RegisterBased factorRegister;
	private int factor;
	private int temporaryStackOffset = 0;

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
	public void setTemporaryStackOffset(int temporaryStackOffset) {
		this.temporaryStackOffset = temporaryStackOffset;
	}

	@Override
	public String toString() {
		int oldOffset = offset;
		offset += this.temporaryStackOffset;
		// Always use 64 bit register, this are stack addresses.
		String secondRegister = "";
		if (factorRegister != null) {
			if (factor == 1) {
				secondRegister = String.format(",%s", factorRegister.toString());
			} else {
				secondRegister = String.format(",%s,%d", factorRegister.toString(), factor);
			}
		}

		String result;
		if (offset == 0) {
			result = String.format("(%s%s)", register.toString(), secondRegister);
		} else if (offset < 0) {
			result = String.format("-0x%x(%s%s)", -offset, register.toString(), secondRegister);
		} else {
			result = String.format("0x%x(%s%s)", offset, register.toString(), secondRegister);
		}
		offset = oldOffset;
		return result;
	}

	public int getOffset() {
		return offset;
	}

	@Override
	public Set<RegisterBased> getReadRegistersOnRightSide() {
		return getReadRegisters();
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		if (factorRegister == null) {
			return Utils.unionSet(register);
		} else {
			return Utils.unionSet(register, factorRegister);
		}
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

	@Override
	public boolean isSpilled() {
		return true;
	}

	@Override
	public Bit getMode() {
		return null;
	}

	@Override
	public SingleRegister getSingleRegister() {
		return null;
	}

	@Override
	public RegisterBundle getRegisterBundle() {
		return null;
	}

	@Override
	public MemoryPointer getMemoryPointer() {
		return this;
	}
}
