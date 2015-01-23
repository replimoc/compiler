package compiler.firm.backend.registerallocation;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

public class AllocationResult {

	public final LinkedList<VirtualRegister> spilledRegisters;
	public final LinkedHashSet<RegisterBundle> usedRegisters;

	public AllocationResult(LinkedList<VirtualRegister> spilledRegisters, LinkedHashSet<RegisterBundle> usedRegisters) {
		this.spilledRegisters = spilledRegisters;
		this.usedRegisters = usedRegisters;
	}
}