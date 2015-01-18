package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

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

	private final LinkedList<SingleRegister>[] allowedRegisters;
	private final boolean isMainMethod;

	private final List<AssemblerOperation> operations;

	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();
	private final byte[] registerUsages = new byte[RegisterBundle.REGISTER_COUNTER];
	private final HashMap<VirtualRegister, SingleRegister> usedRegisters = new HashMap<>();
	private final HashMap<RegisterBundle, LinkedList<VirtualRegister>> partialAllocatedRegisters = new HashMap<>();

	private int currentStackOffset = 0;

	public LinearScanRegisterAllocation(RegisterAllocationPolicy registerPolicy, boolean isMain, List<AssemblerOperation> operations) {
		this.operations = operations;
		this.isMainMethod = isMain;
		this.allowedRegisters = registerPolicy.getAllowedRegisters();
	}

	public void allocateRegisters() {
		calculateRegisterLivetime();

		detectPartiallyAllocatedRegisters();
		assignRegisters();

		setStackSize(currentStackOffset);

		// for (VirtualRegister register : virtualRegisters) {
		// System.out.println("VR" + register.getNum() + " from " + register.getFirstOccurrence() + " to " + register.getLastOccurrence());
		// }

		int line = 0;
		for (AssemblerOperation operation : operations) {
			if (operation instanceof CallOperation) {
				setOperationAliveRegisters(line, (CallOperation) operation);
			}
			if (operation instanceof MethodStartEndOperation) {
				((MethodStartEndOperation) operation).setMain(isMainMethod);
			}
			line++;
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

	private void assignRegisters() {
		sortRegisterListByStart(virtualRegisters);

		for (VirtualRegister virtualRegister : virtualRegisters) {
			if (virtualRegister.getRegister() != null) {
				continue; // register already defined => nothing to do here
			}

			int firstOccurrence = virtualRegister.getFirstOccurrence();
			RegisterBundle registerFreedInCurrLine = freeOutdatedRegistersForLine(firstOccurrence);

			SingleRegister freeRegister = null;
			if (registerFreedInCurrLine != null
					&& isNormalRegisterOrPartiallyAllocatable(registerFreedInCurrLine, firstOccurrence, virtualRegister.getLastOccurrence())) {
				freeRegister = registerFreedInCurrLine.getRegister(virtualRegister.getMode());

				if (partialAllocatedRegisters.containsKey(registerFreedInCurrLine)) {
					virtualRegister.setForceRegister(true);
				}
			} else {
				freeRegister = getAllocatableRegister(virtualRegister);
			}

			if (freeRegister != null) {
				virtualRegister.setStorage(freeRegister);
				usedRegisters.put(virtualRegister, freeRegister);
				registerUsages[freeRegister.getRegisterBundle().getRegisterId()] |= freeRegister.getMask();

			}
		}
	}

	private boolean isNormalRegisterOrPartiallyAllocatable(RegisterBundle registerFreedInCurrLine, int firstOccurrence, int lastOccurrence) {
		LinkedList<VirtualRegister> interferringRegisters = partialAllocatedRegisters.get(registerFreedInCurrLine);
		return interferringRegisters == null || isPartiallyAllocatableRegisterFree(firstOccurrence, lastOccurrence, interferringRegisters);
	}

	private RegisterBundle freeOutdatedRegistersForLine(int line) {
		RegisterBundle registerFreedInCurrLine = null;

		Iterator<Entry<VirtualRegister, SingleRegister>> entriesIterator = usedRegisters.entrySet().iterator();
		while (entriesIterator.hasNext()) {
			Entry<VirtualRegister, SingleRegister> registerEntry = entriesIterator.next();
			int lastOccurrence = registerEntry.getKey().getLastOccurrence();
			if (lastOccurrence <= line) {
				SingleRegister register = registerEntry.getValue();
				freeRegister(register);
				entriesIterator.remove();

				if (lastOccurrence == line) {
					registerFreedInCurrLine = register.getRegisterBundle();
				}
			}
		}

		return registerFreedInCurrLine;
	}

	private SingleRegister getAllocatableRegister(VirtualRegister virtualRegister) {
		SingleRegister freeRegister = getFreePartiallyAllocatedRegisterForLifetime(virtualRegister);
		if (freeRegister == null) {
			Bit mode = virtualRegister.getMode();

			freeRegister = getFreeNormalRegister(mode);

			if (freeRegister == null) {
				VirtualRegister register = getRegisterWithLongestLifetime(mode);
				if (register == null) {
					spillRegister(virtualRegister);
					return null;
				}

				spillRegister(register);
				freeRegister = getFreeNormalRegister(mode);
			}
		}

		return freeRegister;
	}

	private SingleRegister getFreeNormalRegister(Bit mode) {
		LinkedList<SingleRegister> registers = allowedRegisters[mode.ordinal()];

		for (SingleRegister register : registers) {
			if (isRegisterFree(register, false)) {
				return register;
			}
		}

		return null;
	}

	private boolean isRegisterFree(SingleRegister register, boolean allowPartiallyAllocated) {
		byte registerUsage = this.registerUsages[register.getRegisterBundle().getRegisterId()];
		return (allowPartiallyAllocated || registerUsage >= 0) && (registerUsage & register.getMask()) == 0;
	}

	private SingleRegister getFreePartiallyAllocatedRegisterForLifetime(VirtualRegister virtualRegister) {
		Bit mode = virtualRegister.getMode();
		int start = virtualRegister.getFirstOccurrence();
		int end = virtualRegister.getLastOccurrence();

		for (Entry<RegisterBundle, LinkedList<VirtualRegister>> registerInfo : partialAllocatedRegisters.entrySet()) {
			RegisterBundle registerBundle = registerInfo.getKey();

			if (isRegisterFree(registerBundle.getRegister(mode), true)
					&& isPartiallyAllocatableRegisterFree(start, end, registerInfo.getValue())) {
				virtualRegister.setForceRegister(true);
				return registerBundle.getRegister(mode);
			}
		}
		return null;
	}

	private boolean isPartiallyAllocatableRegisterFree(int start, int end, LinkedList<VirtualRegister> interferringRegisters) {
		boolean isValid = true;
		for (VirtualRegister register : interferringRegisters) {
			isValid &= (register.getFirstOccurrence() >= end || register.getLastOccurrence() <= start);
		}
		return isValid;
	}

	private void freeRegister(SingleRegister register) {
		RegisterBundle registerBundle = register.getRegisterBundle();
		registerUsages[registerBundle.getRegisterId()] &= ~register.getMask();
	}

	private void spillRegister(VirtualRegister virtualRegister) {
		SingleRegister freedRegister = usedRegisters.get(virtualRegister);
		if (freedRegister != null) {
			usedRegisters.remove(virtualRegister);
			freeRegister(freedRegister);
		}
		virtualRegister.setSpilled(true);
		currentStackOffset += STACK_ITEM_SIZE;
		virtualRegister.setStorage(new MemoryPointer(-currentStackOffset, SingleRegister.RBP));
	}

	private VirtualRegister getRegisterWithLongestLifetime(Bit mode) {
		int lifetime = 0;
		VirtualRegister register = null;
		for (Entry<VirtualRegister, SingleRegister> testRegister : usedRegisters.entrySet()) {
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
				if (lineOfLabel != null) { // we already had this label => loop found
					expandRegisterUsage(lineOfLabel, line);
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
	}

	private void expandRegisterUsage(int startOperation, int endOperation) {
		for (VirtualRegister register : virtualRegisters) {
			if (register.isAliveAt(startOperation)) {
				setOccurrence(register, endOperation);
			}
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

	private void detectPartiallyAllocatedRegisters() {
		for (VirtualRegister virtualRegister : virtualRegisters) {
			if (virtualRegister.getRegister() != null && virtualRegister.getRegister().getClass() == SingleRegister.class) {
				SingleRegister register = (SingleRegister) virtualRegister.getRegister();
				RegisterBundle registerBundle = register.getRegisterBundle();

				if (!partialAllocatedRegisters.containsKey(registerBundle)) {
					partialAllocatedRegisters.put(registerBundle, new LinkedList<VirtualRegister>());
					registerUsages[registerBundle.getRegisterId()] = (byte) 0x80;
				}
				partialAllocatedRegisters.get(registerBundle).add(virtualRegister);
			}
		}
	}

	private void setStackSize(int size) {
		size += 0x10;
		size &= -0x10; // Align to 8-byte.

		for (AssemblerOperation operation : operations) {
			if (operation instanceof MethodStartEndOperation) {
				MethodStartEndOperation reserveStackOperation = (MethodStartEndOperation) operation;
				reserveStackOperation.setStackOperationSize(size);
			}
		}
	}

	private void setOperationAliveRegisters(int num, CallOperation operation) {
		List<VirtualRegister> registers = new LinkedList<VirtualRegister>();
		for (VirtualRegister register : virtualRegisters) {
			if (register.isAliveAt(num)) {
				registers.add(register);
			}
		}
		operation.addUsedRegisters(registers);
	}

}
