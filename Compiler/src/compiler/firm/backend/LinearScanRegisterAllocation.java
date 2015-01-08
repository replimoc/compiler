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
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.dummy.FreeStackOperation;
import compiler.firm.backend.operations.dummy.ReserveStackOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private final List<AssemblerOperation> operations;
	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();

	private static final int STACK_ITEM_SIZE = 8;
	private int currentStackOffset = 0;

	private LinkedList<Register> freeRegisters = new LinkedList<Register>(
			Arrays.asList(Register._BX, Register._CX, Register._DX,
					Register._8D, Register._9D, Register._12D, Register._13D,
					Register._14D, Register._15D)
			);
	private HashMap<Register, LinkedList<VirtualRegister>> partialAllocatedRegisters = new HashMap<>();

	private HashMap<VirtualRegister, Register> usedRegister = new HashMap<>();

	public LinearScanRegisterAllocation(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	public void allocateRegisters() {
		calculateRegisterLivetime();
		sortRegisterListByStart(virtualRegisters);
		readPartialAllocatedRegisters();
		assignRegisters();
		setStackSize(currentStackOffset);
		// for (VirtualRegister register : virtualRegisters) {
		// System.out.println(register);
		// }
	}

	private void sortRegisterListByStart(List<VirtualRegister> registers) {
		Collections.sort(registers, new Comparator<VirtualRegister>() {
			@Override
			public int compare(VirtualRegister o1, VirtualRegister o2) {
				return o1.getFirstOccurrence() - o2.getFirstOccurrence();
			}
		});
	}

	private Register allocateRegister(VirtualRegister virtualRegister) {
		Register freeRegister = getFreeRegisterForLifetime(virtualRegister);
		if (freeRegister == null) {
			if (this.freeRegisters.isEmpty()) {
				VirtualRegister register = getRegisterWithLongestLifetime();
				if (register == null) {
					spillRegister(virtualRegister);
					return null;
				}

				spillRegister(register);
			}
			freeRegister = this.freeRegisters.pop();
		}
		virtualRegister.setStorage(freeRegister);
		return freeRegister;
	}

	private Register getFreeRegisterForLifetime(VirtualRegister virtualRegister) {
		int start = virtualRegister.getFirstOccurrence();
		int end = virtualRegister.getLastOccurrence();
		for (Entry<Register, LinkedList<VirtualRegister>> registerInfo : partialAllocatedRegisters.entrySet()) {
			boolean isValid = true;
			for (VirtualRegister register : registerInfo.getValue()) {
				isValid &= (register.getFirstOccurrence() > end ||
						register.getLastOccurrence() < start);
			}
			if (isValid) {
				virtualRegister.setForceRegister(true);
				registerInfo.getValue().add(virtualRegister);
				return registerInfo.getKey();
			}
		}
		return null;
	}

	private void freeRegister(Register register) {
		if (!this.freeRegisters.contains(register)
				&& !this.partialAllocatedRegisters.containsKey(register)) {
			this.freeRegisters.push(register);
		}
	}

	private void spillRegister(VirtualRegister virtualRegister) {
		Register freeRegister = usedRegister.get(virtualRegister);
		if (freeRegister != null) {
			usedRegister.remove(virtualRegister);
			this.freeRegisters.push(freeRegister);
		}
		virtualRegister.setSpilled(true);
		currentStackOffset += STACK_ITEM_SIZE;
		virtualRegister.setStorage(new MemoryPointer(-currentStackOffset, Register._BP));
	}

	private VirtualRegister getRegisterWithLongestLifetime() {
		int lifetime = 0;
		VirtualRegister register = null;
		for (Entry<VirtualRegister, Register> testRegister : usedRegister.entrySet()) {
			VirtualRegister virtualRegister = testRegister.getKey();
			if (virtualRegister.getLastOccurrence() >= lifetime && !virtualRegister.isForceRegister()) {
				lifetime = virtualRegister.getLastOccurrence();
				register = virtualRegister;
			}
		}
		return register;
	}

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
				if (lineOfLabel != null) { // we already had this label => loop detected
					expandRegisterUsage(lineOfLabel, line);
					System.out.println("loop at start " + labelOperation.getName() + ":  " + lineOfLabel + " end " + line);
				}
			}

			for (RegisterBased register : operation.getReadRegisters()) {
				setOccurrence(register, line);
			}
			for (RegisterBased register : operation.getWriteRegisters()) {
				setOccurrence(register, line);
			}
			line++;
		}

		// for (VirtualRegister register : virtualRegisters) {
		// System.out.println(register);
		// }
	}

	private void expandRegisterUsage(int startOperation, int endOperation) {
		for (VirtualRegister register : virtualRegisters) { // TODO @Valentin Zickner: make this work instead of your code
			if (register.isAliveAt(startOperation)) {
				setOccurrence(register, endOperation);
			}
		}

		// List<RegisterBased> writeRegisters = new ArrayList<RegisterBased>();
		// for (int i = startOperation; i < endOperation; i++) {
		// AssemblerOperation operation = operations.get(i);
		//
		// // TODO: The next loop should not be necessary. But without this loop it fails for fannkuch.mj.
		// // Maybe there is somewhere a missing read register.
		// for (RegisterBased register : operation.getWriteRegisters()) {
		// if (!writeRegisters.contains(register)) {
		// setOccurrence(register, startOperation);
		// setOccurrence(register, endOperation);
		// }
		// }
		// for (RegisterBased register : operation.getReadRegisters()) {
		// if (!writeRegisters.contains(register)) {
		// setOccurrence(register, startOperation);
		// setOccurrence(register, endOperation);
		// }
		// }
		// writeRegisters.addAll(Arrays.asList(operation.getWriteRegisters()));
		// }
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

	private void assignRegisters() {
		for (VirtualRegister register : virtualRegisters) {
			freeRegistersForLine(register.getFirstOccurrence());

			if (register.getRegister() == null) {
				Register systemRegister = allocateRegister(register);
				if (systemRegister != null) {
					usedRegister.put(register, systemRegister);
				}
			}
		}
	}

	private void readPartialAllocatedRegisters() {
		for (VirtualRegister virtualRegister : virtualRegisters) {
			if (virtualRegister.getRegister() != null && virtualRegister.getRegister().getClass() == Register.class) {
				Register register = (Register) virtualRegister.getRegister();
				if (!partialAllocatedRegisters.containsKey(register)) {
					if (freeRegisters.contains(register)) {
						freeRegisters.remove(register);
					} else {
						// Do not use this register. It is not marked as register allocation register.
						continue;
					}
					partialAllocatedRegisters.put(register, new LinkedList<VirtualRegister>());

				}
				partialAllocatedRegisters.get(virtualRegister.getRegister()).add(virtualRegister);
			}
		}
	}

	private void freeRegistersForLine(int line) {
		List<VirtualRegister> removeRegisters = new ArrayList<>();
		for (Entry<VirtualRegister, Register> register : usedRegister.entrySet()) {
			if (register.getKey().getLastOccurrence() < line) {
				freeRegister(register.getValue());
				removeRegisters.add(register.getKey());
			}
		}
		for (VirtualRegister register : removeRegisters) {
			usedRegister.remove(register);
		}
	}

	private void setStackSize(int size) {
		size += 0x10;
		size &= -0x10; // Align to 8-byte.
		AssemblerOperation reserveOperation = new SubOperation("stack reservation", Bit.BIT64, new Constant(size), Register._SP);
		AssemblerOperation freeOperation = new AddOperation("stack free", Bit.BIT64, new Constant(size), Register._SP);
		if (size <= 0) {
			reserveOperation = new Comment("no items on stack, skip reservation");
			freeOperation = new Comment("no items on stack, skip free");
		}
		for (AssemblerOperation operation : operations) {
			if (operation.getClass() == ReserveStackOperation.class) {
				ReserveStackOperation reserveStackOperation = (ReserveStackOperation) operation;
				reserveStackOperation.setOperation(reserveOperation);
			} else if (operation.getClass() == FreeStackOperation.class) {
				FreeStackOperation freeStackOperation = (FreeStackOperation) operation;
				freeStackOperation.setOperation(freeOperation);
			}
		}
	}
}
