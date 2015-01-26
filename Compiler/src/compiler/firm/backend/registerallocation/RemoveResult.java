package compiler.firm.backend.registerallocation;

import java.util.List;

import compiler.firm.backend.storage.VirtualRegister;

public class RemoveResult {

	public final List<VirtualRegister> spilledRegisters;
	public final List<VirtualRegister> removedList;

	public RemoveResult(List<VirtualRegister> spilledRegisters, List<VirtualRegister> removedList) {
		this.spilledRegisters = spilledRegisters;
		this.removedList = removedList;
	}
}