package compiler.firm.backend.operations;

import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.VirtualRegister;

public class SpillOperation extends MovOperation {

	public SpillOperation(VirtualRegister spillRegister, MemoryPointer memoryPointer) {
		super("spilling VR_" + spillRegister.getNum() + " to " + memoryPointer, spillRegister, memoryPointer);
	}
}
