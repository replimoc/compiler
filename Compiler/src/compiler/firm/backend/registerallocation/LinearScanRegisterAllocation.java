package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private static final int STACK_ITEM_SIZE = 8;

	private final boolean isMainMethod;
	private final List<AssemblerOperation> operations;

	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();
	private int currentStackOffset = 0;

	public LinearScanRegisterAllocation(boolean isMain, List<AssemblerOperation> operations) {
		this.isMainMethod = isMain;
		this.operations = operations;
	}

	public void allocateRegisters(boolean debugRegisterAllocation, boolean noRegisters) {
		calculateRegisterLivetime();

		InterferenceGraph interferenceGraph = new InterferenceGraph(virtualRegisters);

		RegisterAllocationPolicy policy;
		RemoveResult removeResult;
		if (noRegisters) {
			policy = RegisterAllocationPolicy.NO_REGISTERS;
			int availableRegisters = policy.getNumberOfRegisters(Bit.BIT64);
			removeResult = InterferenceGraph.calculateRemoveListAndSpills(interferenceGraph, availableRegisters);
		} else {
			// try with spill registers
			policy = RegisterAllocationPolicy.A_BP_B_12_13_14_15__DI_SI_D_C_8_9_10_11;
			int availableRegisters = policy.getNumberOfRegisters(Bit.BIT64);
			removeResult = InterferenceGraph.calculateRemoveListAndSpills(interferenceGraph, availableRegisters);
			if (!removeResult.spilledRegisters.isEmpty()) { // if we need to spill, we can't use the spill registers
				policy = RegisterAllocationPolicy.A_BP_B_12_13_14_15__DI_SI_D_C_8;
				availableRegisters = policy.getNumberOfRegisters(Bit.BIT64);
				removeResult = InterferenceGraph.calculateRemoveListAndSpills(interferenceGraph, availableRegisters);
			}
		}

		LinkedHashSet<RegisterBundle> usedRegisters = InterferenceGraph
				.allocateRegisters(interferenceGraph, policy, removeResult.removedList);

		spillRegisters(removeResult.spilledRegisters);
		setDummyOperationsInformation(usedRegisters);
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
				// expandRegisterUsage(lineOfLabel, line);
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

	private void spillRegisters(List<VirtualRegister> spilledRegisters) {
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
