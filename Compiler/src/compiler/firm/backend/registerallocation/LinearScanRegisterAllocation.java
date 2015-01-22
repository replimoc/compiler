package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.registerallocation.InterferenceGraph.AllocationResult;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private static final int STACK_ITEM_SIZE = 8;

	private final RegisterAllocationPolicy registerPolicy;
	private final boolean isMainMethod;
	private final List<AssemblerOperation> operations;

	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();
	private int currentStackOffset = 0;

	public LinearScanRegisterAllocation(RegisterAllocationPolicy registerPolicy, boolean isMain, List<AssemblerOperation> operations) {
		this.registerPolicy = registerPolicy;
		this.isMainMethod = isMain;
		this.operations = operations;
	}

	public void allocateRegisters(boolean debugRegisterAllocation) {
		calculateRegisterLivetime();

		InterferenceGraph interferenceGraph = new InterferenceGraph(virtualRegisters);
		try {
			AllocationResult allocationResult = interferenceGraph.allocateRegisters(registerPolicy);

			spillRegisters(allocationResult.spilledRegisters);
			setDummyOperationsInformation(allocationResult.usedRegisters);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	// ---------------------- calculate register livetime -------------------

	private void calculateRegisterLivetime() {
		int line = 0;
		HashMap<LabelOperation, Integer> passedLabels = new HashMap<>();
		for (AssemblerOperation operation : operations) {
			if (operation instanceof LabelOperation) {
				passedLabels.put((LabelOperation) operation, line);
			}
			if (operation instanceof JumpOperation) {
				LabelOperation labelOperation = ((JumpOperation) operation).getLabel();
				Integer lineOfLabel = passedLabels.get(labelOperation);
				if (lineOfLabel != null) { // we already had this label => loop found
					expandRegisterUsage(lineOfLabel, line);
				}
			}

			for (RegisterBased register : operation.getReadRegisters()) {
				setOccurrence(register, line, true);
			}
			for (RegisterBased register : operation.getWriteRegisters()) {
				setOccurrence(register, line, false);
			}
			line++;
		}
	}

	private void expandRegisterUsage(int startOperation, int endOperation) {
		for (VirtualRegister register : virtualRegisters) {
			if (register.isAliveAt(startOperation)) {
				setOccurrence(register, endOperation, true);
			}
		}
	}

	private void setOccurrence(RegisterBased register, int occurrence, boolean read) {
		if (register != null && register.getClass() == VirtualRegister.class) {
			VirtualRegister virtualRegister = (VirtualRegister) register;
			virtualRegister.expandLifetime(occurrence, read);

			if (!virtualRegisters.contains(virtualRegister)) {
				virtualRegisters.add(virtualRegister);
			}
		}
	}

	private void spillRegisters(LinkedList<VirtualRegister> spilledRegisters) {
		for (VirtualRegister spilledRegister : spilledRegisters) {
			spilledRegister.setSpilled(true);
			currentStackOffset += STACK_ITEM_SIZE;
			spilledRegister.setStorage(new MemoryPointer(currentStackOffset, SingleRegister.RSP));
		}
	}

	// ------------------------------ setting information to dummy operations -----------------

	private void setDummyOperationsInformation(Set<RegisterBundle> usedRegisters) {
		int stackSize = currentStackOffset;
		if (stackSize > 0) {
			stackSize += 0x10;
			stackSize &= -0x10; // Align to 8-byte.
		}

		int line = 0;
		for (AssemblerOperation operation : operations) {
			if (operation instanceof CallOperation) {
				List<VirtualRegister> aliveRegisters = getRegistersAliveAt(line);
				((CallOperation) operation).addAliveRegisters(aliveRegisters);
			}

			if (operation instanceof MethodStartEndOperation) {
				MethodStartEndOperation methodStartEndOperation = (MethodStartEndOperation) operation;
				methodStartEndOperation.setStackOperationSize(stackSize);

				if (isMainMethod) { // if it is the main, no registers need to be saved
					methodStartEndOperation.setUsedRegisters(new HashSet<RegisterBundle>());
				} else {
					methodStartEndOperation.setUsedRegisters(usedRegisters);
				}
			}
			line++;
		}
	}

	private List<VirtualRegister> getRegistersAliveAt(int num) {
		List<VirtualRegister> registers = new LinkedList<VirtualRegister>();
		for (VirtualRegister register : this.virtualRegisters) {
			if (register.isAliveAt(num)) {
				registers.add(register);
			}
		}
		return registers;
	}
}
