package compiler.firm.backend.operations;

import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.VirtualRegister;

public class ReloadOperation extends MovOperation {

	public ReloadOperation(MemoryPointer memoryPointer, VirtualRegister reloadRegister) {
		super("reloading original: " + reloadRegister, memoryPointer, reloadRegister);
	}
}
