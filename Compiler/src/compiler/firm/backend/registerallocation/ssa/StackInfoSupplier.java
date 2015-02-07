package compiler.firm.backend.registerallocation.ssa;

import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.VirtualRegister;

public interface StackInfoSupplier {
	MemoryPointer getStackLocation(VirtualRegister register);

	MemoryPointer allocateStackLocation(VirtualRegister register);
}
