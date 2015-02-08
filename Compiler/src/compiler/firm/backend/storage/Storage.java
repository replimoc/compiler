package compiler.firm.backend.storage;

import java.util.Set;

import compiler.firm.backend.Bit;

public abstract class Storage {
	@Override
	public abstract String toString();

	public abstract Set<RegisterBased> getReadRegisters();

	public abstract Set<RegisterBased> getWriteRegisters();

	public abstract Set<RegisterBased> getReadRegistersOnRightSide();

	public abstract boolean isSpilled();

	public abstract Bit getMode();

	public abstract SingleRegister getSingleRegister();

	public abstract RegisterBundle getRegisterBundle();

	public abstract MemoryPointer getMemoryPointer();

	public abstract void setTemporaryStackOffset(int temporaryStackOffset);
}
