package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

public abstract class Storage {
	public abstract String toString(Bit bit);

	public abstract RegisterBased getUsedRegister();

	public abstract RegisterBased getReadOnRightSideRegister();
}
