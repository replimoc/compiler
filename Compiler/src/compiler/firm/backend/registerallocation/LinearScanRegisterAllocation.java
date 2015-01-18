package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

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

	private final SingleRegister[][] allowedRegisters;
	private final boolean isMainMethod;

	private final List<AssemblerOperation> operations;

	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();
	private final byte[] registerUsages = new byte[RegisterBundle.REGISTER_COUNTER];
	private final HashMap<VirtualRegister, SingleRegister> currentlyUsedRegisters = new HashMap<>();
	private final HashMap<RegisterBundle, LinkedList<VirtualRegister>> partialAllocatedRegisters = new HashMap<>();
	private final HashSet<RegisterBundle> usedRegisters = new HashSet<>();

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

		// for (VirtualRegister register : virtualRegisters) {
		// System.out.println("VR" + register.getNum() + " from " + register.getFirstOccurrence() + " to " + register.getLastOccurrence());
		// }

		setDummyOperationsInformation();
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

			SingleRegister allocatableRegister = null;
			if (registerFreedInCurrLine != null) {
				SingleRegister freedRegister = registerFreedInCurrLine.getRegister(virtualRegister.getMode());

				if (isRegisterFree(freedRegister, firstOccurrence, virtualRegister.getLastOccurrence())) {
					allocatableRegister = freedRegister;
				}
			}
			if (allocatableRegister == null) {
				allocatableRegister = getAllocatableRegister(virtualRegister);
			}

			if (allocatableRegister != null) {
				allocateRegister(virtualRegister, allocatableRegister);
			}
		}
	}

	private void allocateRegister(VirtualRegister virtualRegister, SingleRegister allocatableRegister) {
		virtualRegister.setStorage(allocatableRegister);
		currentlyUsedRegisters.put(virtualRegister, allocatableRegister);
		RegisterBundle registerBundle = allocatableRegister.getRegisterBundle();
		registerUsages[registerBundle.getRegisterId()] |= allocatableRegister.getMask();

		if (partialAllocatedRegisters.containsKey(registerBundle)) {
			virtualRegister.setForceRegister(true);
		}
		usedRegisters.add(registerBundle);
	}

	private RegisterBundle freeOutdatedRegistersForLine(int line) {
		RegisterBundle registerFreedInCurrLine = null;

		Iterator<Entry<VirtualRegister, SingleRegister>> entriesIterator = currentlyUsedRegisters.entrySet().iterator();
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
		Bit mode = virtualRegister.getMode();
		SingleRegister freeRegister = getFreeRegister(virtualRegister);

		if (freeRegister == null) {
			VirtualRegister register = getRegisterWithLongestLifetime(mode);
			if (register == null) {
				spillRegister(virtualRegister);
				return null;
			}

			spillRegister(register);
			freeRegister = getFreeRegister(virtualRegister);
		}

		return freeRegister;
	}

	private SingleRegister getFreeRegister(VirtualRegister virtualRegister) {
		SingleRegister[] registers = allowedRegisters[virtualRegister.getMode().ordinal()];

		for (SingleRegister register : registers) {
			if (isRegisterFree(register, virtualRegister.getFirstOccurrence(), virtualRegister.getLastOccurrence())) {
				return register;
			}
		}

		return null;
	}

	private boolean isRegisterFree(SingleRegister register, int start, int end) {
		byte registerUsage = this.registerUsages[register.getRegisterBundle().getRegisterId()];

		boolean currentlyUsed = (registerUsage & register.getMask()) != 0;
		if (currentlyUsed) {
			return false;
		}

		if (registerUsage < 0) { // partially allocated register
			return isPartiallyAllocatableRegisterFree(start, end, partialAllocatedRegisters.get(register.getRegisterBundle()));
		} else {
			return true;
		}
	}

	private boolean isPartiallyAllocatableRegisterFree(int start, int end, LinkedList<VirtualRegister> interferringRegisters) {
		boolean isFree = true;
		for (VirtualRegister register : interferringRegisters) {
			isFree &= (register.getFirstOccurrence() >= end || register.getLastOccurrence() <= start);
		}
		return isFree;
	}

	private void freeRegister(SingleRegister register) {
		RegisterBundle registerBundle = register.getRegisterBundle();
		registerUsages[registerBundle.getRegisterId()] &= ~register.getMask();
	}

	private void spillRegister(VirtualRegister virtualRegister) {
		SingleRegister freedRegister = currentlyUsedRegisters.get(virtualRegister);
		if (freedRegister != null) {
			currentlyUsedRegisters.remove(virtualRegister);
			freeRegister(freedRegister);
		}
		virtualRegister.setSpilled(true);
		currentStackOffset += STACK_ITEM_SIZE;
		virtualRegister.setStorage(new MemoryPointer(-currentStackOffset, SingleRegister.RBP));
	}

	private VirtualRegister getRegisterWithLongestLifetime(Bit mode) {
		int lifetime = 0;
		VirtualRegister register = null;
		for (Entry<VirtualRegister, SingleRegister> testRegister : currentlyUsedRegisters.entrySet()) {
			VirtualRegister virtualRegister = testRegister.getKey();
			if (!virtualRegister.isForceRegister() && virtualRegister.getLastOccurrence() >= lifetime) {
				lifetime = virtualRegister.getLastOccurrence();
				register = virtualRegister;
			}
		}
		return register;
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

	// ------------------------------ setting information to dummy operations -----------------

	private void setDummyOperationsInformation() {
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
