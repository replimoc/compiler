package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public abstract class Storage {
	@Override
	public abstract String toString();

	public abstract RegisterBased[] getUsedRegister();

	public abstract RegisterBased[] getReadOnRightSideRegister();

	public abstract boolean isSpilled();

	public abstract Bit getMode();

	public abstract SingleRegister getSingleRegister();

	public abstract RegisterBundle getRegisterBundle();

	public abstract MemoryPointer getMemoryPointer();
}
