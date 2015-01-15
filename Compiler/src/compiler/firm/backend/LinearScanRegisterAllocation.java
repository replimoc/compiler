package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.AddOperation;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.Comment;
import compiler.firm.backend.operations.LabelOperation;
import compiler.firm.backend.operations.SubOperation;
import compiler.firm.backend.operations.dummy.FreeStackOperation;
import compiler.firm.backend.operations.dummy.ReserveStackOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.JumpOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

public class LinearScanRegisterAllocation {
	private static final int STACK_ITEM_SIZE = 8;

	private final List<AssemblerOperation> operations;
	private final ArrayList<VirtualRegister> virtualRegisters = new ArrayList<VirtualRegister>();

	private int currentStackOffset = 0;

	@SuppressWarnings("unchecked")
	private LinkedList<SingleRegister> allowedRegisters[] = new LinkedList[] {
			// 64bit registers
			getList(SingleRegister.RAX, SingleRegister.RBX, SingleRegister.RCX, SingleRegister.RDX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11, SingleRegister.R12, SingleRegister.R13,
					SingleRegister.RDI, SingleRegister.RSI),
			// 32 bit registers
			getList(SingleRegister.EAX, SingleRegister.EBX, SingleRegister.ECX, SingleRegister.EDX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D, SingleRegister.R12D, SingleRegister.R13D,
					SingleRegister.EDI, SingleRegister.ESI),
			// 8 bit registers
			getList(SingleRegister.AL, SingleRegister.BL, SingleRegister.CL, SingleRegister.DL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B, SingleRegister.R12B, SingleRegister.R13B,
					SingleRegister.DIL, SingleRegister.SIL)
	};

	private final byte[] registerUsages = new byte[RegisterBundle.REGISTER_COUNTER];
	private final HashMap<VirtualRegister, SingleRegister> usedRegisters = new HashMap<>();

	private HashMap<RegisterBundle, LinkedList<VirtualRegister>> partialAllocatedRegisters = new HashMap<>();

	public LinearScanRegisterAllocation(List<AssemblerOperation> operations) {
		this.operations = operations;
	}

	private LinkedList<SingleRegister> getList(SingleRegister... registers) {
		return new LinkedList<>(Arrays.asList(registers));
	}

	public void allocateRegisters() {
		calculateRegisterLivetime();

		detectPartiallyAllocatedRegisters();
		assignRegisters();

		setStackSize(currentStackOffset);
		// for (VirtualRegister register : virtualRegisters) {
		// System.out.println(register);
		// }
		int i = 0;
		for (AssemblerOperation operation : operations) {
			if (operation instanceof CallOperation) {
				setOperationAliveRegisters(i, (CallOperation) operation);
			}
			i++;
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
			isValid &= (register.getFirstOccurrence() > end || register.getLastOccurrence() < start);
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
		AssemblerOperation reserveOperation = new SubOperation("stack reservation", new Constant(size), SingleRegister.RSP);
		AssemblerOperation freeOperation = new AddOperation("stack free", new Constant(size), SingleRegister.RSP);
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
