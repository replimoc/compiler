package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.dummy.FreeStackOperation;
import compiler.firm.backend.operations.dummy.ReserveStackOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.StackPointer;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private final List<AssemblerOperation> operations;
	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();

	private static final int STACK_ITEM_SIZE = 8;
	private int currentStackOffset;

	private LinkedList<Register> freeRegisters = new LinkedList<Register>(
			Arrays.asList(Register._13D, Register._14D, Register._15D)
			);

	private HashMap<VirtualRegister, Storage> usedRegister = new HashMap<>();
	private HashMap<VirtualRegister, StackPointer> usedStack = new HashMap<>();

	public LinearScanRegisterAllocation(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	public void allocateRegisters() {
		fillRegisterList();
		createStackPointerForRegisters();
		List<VirtualRegister> registerSortedByEnd = new ArrayList<>(virtualRegisters);
		sortRegisterListByStart(virtualRegisters);
		setStackSize(virtualRegisters.size());
		sortRegisterListByEnd(registerSortedByEnd);
		int maximumRegisters = getMaximumNumberOfRegisters(virtualRegisters, registerSortedByEnd);

		System.out.println("maximum registers: " + maximumRegisters);
		assignRegisters();
		for (VirtualRegister register : virtualRegisters) {
			System.out.println(register + " -> " + register.getRegister());
		}
	}

	private void sortRegisterListByStart(List<VirtualRegister> registers) {
		Collections.sort(registers, new Comparator<VirtualRegister>() {
			@Override
			public int compare(VirtualRegister o1, VirtualRegister o2) {
				return o1.getFirstOccurrence() - o2.getFirstOccurrence();
			}
		});
	}

	private void sortRegisterListByEnd(List<VirtualRegister> registers) {
		Collections.sort(registers, new Comparator<VirtualRegister>() {
			@Override
			public int compare(VirtualRegister o1, VirtualRegister o2) {
				return o1.getLastOccurrence() - o2.getLastOccurrence();
			}
		});
	}

	private Storage allocateRegister(VirtualRegister virtualRegister) {
		if (!this.freeRegisters.isEmpty()) {
			return this.freeRegisters.pop();
		} else {
			// TODO: Spill register with longest lifetime
			virtualRegister.setSpilled(true);
			return usedStack.get(virtualRegister);
		}
	}

	private void freeRegister(Register register) {
		if (!this.freeRegisters.contains(register)) {
			this.freeRegisters.push(register);
		}
	}

	private VirtualRegister getRegisterWithLongestLifetime() {
		int lifetime = 0;
		VirtualRegister register = null;
		for (Entry<VirtualRegister, Storage> testRegister : usedRegister.entrySet()) {
			VirtualRegister virtualRegister = testRegister.getKey();
			if (virtualRegister.getLastOccurrence() >= lifetime) {
				lifetime = virtualRegister.getLastOccurrence();
				register = virtualRegister;
			}
		}
		return register;
	}

	private void fillRegisterList() {
		int line = 0;
		List<LabelOperation> passedLabels = new LinkedList<>();
		for (AssemblerOperation operation : operations) {
			if (operation instanceof LabelOperation) {
				passedLabels.add((LabelOperation) operation);
			}
			if (operation instanceof JumpOperation) {
				LabelOperation labelOperation = ((JumpOperation) operation).getLabel();
				if (passedLabels.contains(labelOperation)) {
					int startOperation = operations.indexOf(labelOperation);
					System.out.println("Loop detected: " + startOperation + " -> " + line);
					expandRegisterUsage(startOperation, line);
				}
			}

			for (RegisterBased register : operation.getUsedRegisters()) {
				setOccurrence(register, line);
			}
			line++;
		}
	}

	private void createStackPointerForRegisters() {
		for (VirtualRegister virtualRegister : virtualRegisters) {
			currentStackOffset -= STACK_ITEM_SIZE;
			usedStack.put(virtualRegister, new StackPointer(currentStackOffset, Register._BP));
		}
	}

	private void expandRegisterUsage(int startOperation, int endOperation) {
		List<RegisterBased> writeRegisters = new ArrayList<RegisterBased>();
		for (int i = startOperation; i < endOperation; i++) {
			AssemblerOperation operation = operations.get(i);
			for (RegisterBased register : operation.getUsedRegisters()) {
				if (!writeRegisters.contains(register)) {
					setOccurrence(register, startOperation);
					setOccurrence(register, endOperation);
				}
			}
			writeRegisters.addAll(Arrays.asList(operation.getUsedRegisters()));
		}
	}

	private void setOccurrence(RegisterBased register, int occurrence) {
		if (register != null && register.getClass() == VirtualRegister.class) {
			VirtualRegister virtualRegister = (VirtualRegister) register;
			virtualRegister.setOccurrence(occurrence);

			if (!virtualRegisters.contains(virtualRegister)) {
				virtualRegisters.add(virtualRegister);
			}
		}
	}

	private int getMaximumNumberOfRegisters(List<VirtualRegister> sortByStart, List<VirtualRegister> sortByEnd) {
		if (sortByStart.size() == 0) {
			return 0;
		}

		int maximumRegisters = 0;
		int currentRegisters = 0;

		int startRegisterIndex = 0;
		int endRegisterIndex = 0;

		while (startRegisterIndex < sortByStart.size()) {
			int startRegisterOccurrence = sortByStart.get(startRegisterIndex).getFirstOccurrence();
			int endRegisterOccurrence = sortByEnd.get(endRegisterIndex).getLastOccurrence();

			if (startRegisterOccurrence <= endRegisterOccurrence) {
				currentRegisters++;
				maximumRegisters = Math.max(maximumRegisters, currentRegisters);
				startRegisterIndex++;
			} else {
				currentRegisters--;
				endRegisterIndex++;
			}
		}
		return maximumRegisters;
	}

	private void assignRegisters() {
		for (VirtualRegister register : virtualRegisters) {
			freeRegistersForLine(register.getFirstOccurrence());

			if (register.getRegister() == null) {
				Storage systemRegister = allocateRegister(register);
				register.setStorage(systemRegister);
				usedRegister.put(register, systemRegister);
			} else {
				// TODO: Reserve this register
			}
		}
	}

	private void freeRegistersForLine(int line) {
		List<VirtualRegister> removeRegisters = new ArrayList<>();
		for (Entry<VirtualRegister, Storage> register : usedRegister.entrySet()) {
			if (register.getKey().getLastOccurrence() < line && register.getValue().getClass() == Register.class) {
				freeRegister((Register) register.getValue());
				removeRegisters.add(register.getKey());
			}
		}
		for (VirtualRegister register : removeRegisters) {
			usedRegister.remove(register);
		}
	}

	private void setStackSize(int size) {
		for (AssemblerOperation operation : operations) {
			if (operation.getClass() == ReserveStackOperation.class) {
				ReserveStackOperation reserveStackOperation = (ReserveStackOperation) operation;
				reserveStackOperation.setOperation(new SubOperation("stack reservation", Bit.BIT64,
						new Constant(StorageManagement.STACK_ITEM_SIZE * size), Register._SP));
			} else if (operation.getClass() == FreeStackOperation.class) {
				FreeStackOperation freeStackOperation = (FreeStackOperation) operation;
				freeStackOperation.setOperation(new AddOperation("stack free", Bit.BIT64,
						new Constant(StorageManagement.STACK_ITEM_SIZE * size), Register._SP));
			}
		}
	}
}
