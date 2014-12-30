package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
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
		List<VirtualRegister> registerSortedByEnd = new ArrayList<>(virtualRegisters);
		sortRegisterList(virtualRegisters, 1);
		sortRegisterList(registerSortedByEnd, -1);
		int maximumRegisters = getMaximumNumberOfRegisters(virtualRegisters, registerSortedByEnd);
		System.out.println("maximum registers: " + maximumRegisters);
		assignRegisters();
	}

	private void sortRegisterList(List<VirtualRegister> registers, final int multiplicator) {
		Collections.sort(registers, new Comparator<VirtualRegister>() {
			@Override
			public int compare(VirtualRegister o1, VirtualRegister o2) {
				return multiplicator * (o1.getFirstOccurrence() > o2.getFirstOccurrence() ? 1 : -1);
			}
		});
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

	private void expandRegisterUsage(int startOperation, int endOperation) {
		List<RegisterBased> writeRegisters = new ArrayList<RegisterBased>();
		for (int i = startOperation; i < endOperation; i++) {
			AssemblerOperation operation = operations.get(i);
			for (RegisterBased register : operation.getReadRegisters()) {
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
		for (VirtualRegister register : virtualRegisters) {
			System.out.println(register + " between " + register.getFirstOccurrence() + " and " + register.getLastOccurrence());
		}
		return maximumRegisters;
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
