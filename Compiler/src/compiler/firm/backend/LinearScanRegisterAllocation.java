package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private final List<AssemblerOperation> operations;
	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();

	private LinkedList<Register> freeRegisters = new LinkedList<Register>(
			Arrays.asList(Register._AX, Register._BX, Register._CX, Register._DX));

	private HashMap<Register, VirtualRegister> usedRegister = new HashMap<>();

	public LinearScanRegisterAllocation(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	public void allocateRegisters() {
		fillRegisterList();
		// TODO: Is sorting of registers necessary?
		assignRegisters();
	}

	private Register allocateRegister() {
		if (!this.freeRegisters.isEmpty()) {
			return this.freeRegisters.pop();
		} else {
			// TODO Create spill code
			throw new RuntimeException("Spillcode implementation not implemented yet");
		}
	}

	private void freeRegister(Register register) {
		if (!this.freeRegisters.contains(register)) {
			this.freeRegisters.push(register);
		}
	}

	private void fillRegisterList() {
		int line = 0;
		for (AssemblerOperation operation : operations) {
			for (RegisterBased register : operation.getUsedRegisters()) {
				if (register != null && register.getClass() == VirtualRegister.class) {
					VirtualRegister virtualRegister = (VirtualRegister) register;
					// TODO In loops this must be the end of the loop, not the last line in the loop
					virtualRegister.setOccurrence(line);
					if (!virtualRegisters.contains(virtualRegister)) {
						virtualRegisters.add(virtualRegister);
					}
				}
			}
			line++;
		}
	}

	private void assignRegisters() {
		for (VirtualRegister register : virtualRegisters) {
			freeRegistersForLine(register.getFirstOccurrence());

			if (register.getRegister() == null) {
				Register systemRegister = allocateRegister();
				register.setRegister(systemRegister);
				usedRegister.put(systemRegister, register);
			} else {
				// TODO: Reserve this register
			}
		}
	}

	private void freeRegistersForLine(int line) {
		List<Register> removeRegisters = new ArrayList<>();
		for (Entry<Register, VirtualRegister> register : usedRegister.entrySet()) {
			if (register.getValue().getLastOccurrence() < line) {
				freeRegister(register.getKey());
				removeRegisters.add(register.getKey());
			}
		}
		for (Register register : removeRegisters) {
			usedRegister.remove(register);
		}
	}
}
