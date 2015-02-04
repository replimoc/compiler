package compiler.firm.backend.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.TransferGraphSolver;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.CurrentlyAliveRegistersNeeding;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;
import compiler.utils.Utils;

public class CallOperation extends AssemblerOperation implements CurrentlyAliveRegistersNeeding {
	private static final int STACK_ITEM_SIZE = 8;

	private final String name;
	private final List<Parameter> parameters;
	private final CallingConvention callingConvention;
	private final List<RegisterBundle> aliveRegisters = new LinkedList<>();
	private final VirtualRegister resultRegister;

	public CallOperation(String comment, Bit mode, String name, List<Parameter> parameters, CallingConvention callingConvention, boolean hasResult) {
		super(comment);
		this.name = name;
		this.parameters = parameters;
		this.callingConvention = callingConvention;
		this.aliveRegisters.add(RegisterBundle._SP);

		if (hasResult) {
			this.resultRegister = new VirtualRegister(mode);
			resultRegister.addPreferedRegister(new VirtualRegister(callingConvention.getReturnRegister().getRegister(mode)));
		} else {
			this.resultRegister = null;
		}
	}

	@Override
	public String getOperationString() {
		return String.format("\tcall %s", name);
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		Set<RegisterBased> readRegisters = new HashSet<>();
		for (Parameter parameter : this.parameters) {
			if (parameter.storage instanceof RegisterBased) {
				readRegisters.add((RegisterBased) parameter.storage);
			}
		}
		return readRegisters;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		if (resultRegister != null) {
			return Utils.<RegisterBased> unionSet(resultRegister);
		} else {
			return Collections.emptySet();
		}
	}

	public VirtualRegister getResult() {
		return resultRegister;
	}

	private List<RegisterBundle> getSaveRegisters() {
		List<RegisterBundle> saveRegisters = new ArrayList<>();

		if (resultRegister != null && !resultRegister.isSpilled() && resultRegister.getRegisterBundle() == callingConvention.getReturnRegister()) {
			aliveRegisters.remove(callingConvention.getReturnRegister()); // if the result register is the return register, this was not alive before
		}

		for (RegisterBundle register : callingConvention.callerSavedRegisters()) {
			if (this.aliveRegisters.contains(register)) {
				saveRegisters.add(register);
			}
		}
		return saveRegisters;
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = new LinkedList<String>();

		List<RegisterBundle> callerSavedRegisters = getSaveRegisters();

		// The following is before filling registers to have no problem with register allocation.
		RegisterBundle[] callingRegisters = callingConvention.getParameterRegisters();
		int numberOfStackParameters = parameters.size() - callingRegisters.length;

		HashMap<RegisterBundle, MemoryPointer> registerStackLocations = new HashMap<>();
		int stackPosition = STACK_ITEM_SIZE * (callerSavedRegisters.size() + Math.max(0, numberOfStackParameters) - 1);
		int maxStackOffset = stackPosition + STACK_ITEM_SIZE;

		// Save all callerSavedRegisters to stack
		for (RegisterBundle saveRegister : callerSavedRegisters) {
			result.add(new PushOperation(saveRegister.getRegister(Bit.BIT64)).toString());
			// maxStackOffset will be added by temporaryStackOffset
			registerStackLocations.put(saveRegister, new MemoryPointer(stackPosition - maxStackOffset, SingleRegister.RSP));
			stackPosition -= STACK_ITEM_SIZE;
		}

		Constant stackAllocationSize = new Constant(STACK_ITEM_SIZE * numberOfStackParameters);

		if (numberOfStackParameters > 0) {
			stackPosition = STACK_ITEM_SIZE * callerSavedRegisters.size();
			// Copy parameters to stack
			for (int i = numberOfStackParameters - 1; i >= 0; i--) {
				Parameter source = parameters.get(i + callingRegisters.length);
				// Copy parameter
				source.storage.setTemporaryStackOffset(stackPosition);
				Storage sourceStorage;

				if (source.storage.getRegisterBundle() != null) { // TODO: make this work with high and low bytes of registers
					sourceStorage = source.storage.getRegisterBundle().getRegister(Bit.BIT64);
				} else {
					sourceStorage = source.storage;
				}

				result.add(new PushOperation(sourceStorage).toString());
				source.storage.setTemporaryStackOffset(0);
				stackPosition += STACK_ITEM_SIZE;
			}
		}

		List<Pair<Storage, RegisterBased>> parameterMoves = new LinkedList<>();
		// Copy parameters in calling registers
		for (int i = 0; i < parameters.size() && i < callingRegisters.length; i++) {
			Parameter source = parameters.get(i);
			RegisterBundle register = callingRegisters[i];
			Storage storage = source.storage;

			if (registerStackLocations.containsKey(storage.getRegisterBundle())) {
				storage = registerStackLocations.get(storage.getRegisterBundle());
			}
			storage.setTemporaryStackOffset(maxStackOffset);

			parameterMoves.add(new Pair<Storage, RegisterBased>(storage, register.getRegister(source.mode)));
		}
		result.addAll(TransferGraphSolver.calculateOperations(parameterMoves));
		for (Pair<Storage, RegisterBased> currMove : parameterMoves) {
			currMove.first.setTemporaryStackOffset(0);
		}

		result.add(toString());
		if (resultRegister != null) {
			result.addAll(Arrays.asList(new MovOperation(callingConvention.getReturnRegister().getRegister(resultRegister.getMode()), resultRegister)
					.toStringWithSpillcode()));
		}

		if (numberOfStackParameters > 0) {
			result.add(new AddOperation(stackAllocationSize, SingleRegister.RSP, SingleRegister.RSP).toString());
		}

		for (int i = callerSavedRegisters.size() - 1; i >= 0; i--) {
			result.add(new PopOperation(callerSavedRegisters.get(i).getRegister(Bit.BIT64)).toString());
		}

		String[] resultStrings = new String[result.size()];
		result.toArray(resultStrings);
		return resultStrings;
	}

	public void addAliveRegisters(List<VirtualRegister> registers) {
		for (VirtualRegister registerBased : registers) {
			Storage storage = registerBased.getRegister();
			if (!(storage instanceof SingleRegister)) {
				continue;
			}
			this.aliveRegisters.add(((SingleRegister) storage).getRegisterBundle());
		}
	}

	@Override
	public void setAliveRegisters(Set<RegisterBundle> registers) {
		this.aliveRegisters.clear();
		this.aliveRegisters.addAll(registers);
	}

	public static class Parameter {
		public final Storage storage;
		public final Bit mode;

		public Parameter(Storage storage, Bit mode) {
			this.storage = storage;
			this.mode = mode;
		}
	}
}
