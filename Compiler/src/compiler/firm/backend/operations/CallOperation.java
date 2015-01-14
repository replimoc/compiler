package compiler.firm.backend.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public class CallOperation extends AssemblerOperation {
	private static final int STACK_ITEM_SIZE = 8;

	private final String name;
	private final List<Parameter> parameters;
	private final CallingConvention callingConvention;
	private List<RegisterBundle> usedRegisters = new LinkedList<>();

	public CallOperation(String name, List<Parameter> parameters, CallingConvention callingConvention) {
		this.name = name;
		this.parameters = parameters;
		this.callingConvention = callingConvention;
		this.usedRegisters.add(RegisterBundle._SP);
		this.usedRegisters.add(RegisterBundle._BP);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcall %s", name);
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased[] readRegister = new RegisterBased[this.parameters.size()];
		int i = 0;
		for (Parameter parameter : this.parameters) {
			if (parameter.storage instanceof RegisterBased) {
				readRegister[i++] = (RegisterBased) parameter.storage;
			}
		}
		return readRegister;
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		return new RegisterBased[] { new VirtualRegister(Bit.BIT64, callingConvention.getReturnRegister()) };
	}

	private List<RegisterBundle> getSaveRegisters() {
		List<RegisterBundle> saveRegisters = new ArrayList<>();
		for (RegisterBundle register : callingConvention.callerSavedRegisters()) {
			if (this.usedRegisters.contains(register)) {
				saveRegisters.add(register);
			}
		}
		return saveRegisters;
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = new LinkedList<String>();

		List<RegisterBundle> callerSavedRegisters = getSaveRegisters();

		// Save all callerSavedRegisters to stack
		for (RegisterBundle saveRegister : callerSavedRegisters) {
			result.add(new PushOperation(Bit.BIT64, saveRegister.getRegister(Bit.BIT64)).toString());
		}

		// The following is before filling registers to have no problem with register allocation.
		RegisterBundle[] callingRegisters = callingConvention.getParameterRegisters();
		int numberOfstackParameters = parameters.size() - callingRegisters.length;
		Constant stackAllocationSize = new Constant(STACK_ITEM_SIZE * numberOfstackParameters);

		if (numberOfstackParameters > 0) {
			result.add(new SubOperation(Bit.BIT64, stackAllocationSize, SingleRegister.RSP).toString());

			RegisterBundle temporaryRegister = getTemporaryRegister();

			// Copy parameters to stack
			for (int i = 0; i < numberOfstackParameters; i++) {
				Parameter source = parameters.get(i + callingRegisters.length);
				// Copy parameter
				MemoryPointer destinationPointer = new MemoryPointer(i * STACK_ITEM_SIZE, SingleRegister.RSP);
				if (source.storage.getClass() == MemoryPointer.class
						|| (source.storage.getClass() == VirtualRegister.class && source.storage.isSpilled())) {
					result.add(new MovOperation(source.mode, source.storage, temporaryRegister.getRegister(source.mode)).toString());
					result.add(new MovOperation(source.mode, temporaryRegister.getRegister(source.mode), destinationPointer).toString());
				} else {
					result.add(new MovOperation(source.mode, source.storage, destinationPointer).toString());
				}
			}
		}

		// Copy parameters in calling registers
		for (int i = 0; i < parameters.size() && i < callingRegisters.length; i++) {
			Parameter source = parameters.get(i);
			RegisterBundle register = callingRegisters[i];
			if (register.getRegister(source.mode) == null) {
				result.add(new MovOperation(Bit.BIT64, source.storage, register.getRegister(Bit.BIT64)).toString());
				// TODO: the mask should be saved in the mode
				result.add(new AndOperation(Bit.BIT64, new Constant(0xFF), register.getRegister(Bit.BIT64)).toString());
			} else {
				result.add(new MovOperation(source.mode, source.storage, register.getRegister(source.mode)).toString());
			}
		}

		result.add(toString());

		if (numberOfstackParameters > 0) {
			result.add(new AddOperation(Bit.BIT64, stackAllocationSize, SingleRegister.RSP).toString());
		}

		for (int i = callerSavedRegisters.size() - 1; i >= 0; i--) {
			result.add(new PopOperation(Bit.BIT64, callerSavedRegisters.get(i).getRegister(Bit.BIT64)).toString());
		}

		String[] resultStrings = new String[result.size()];
		result.toArray(resultStrings);
		return resultStrings;
	}

	public void addUsedRegisters(List<VirtualRegister> registers) {
		for (VirtualRegister registerBased : registers) {
			Storage storage = registerBased.getRegister();
			if (!(storage instanceof SingleRegister)) {
				continue;
			}
			this.usedRegisters.add(((SingleRegister) storage).getRegisterBundle());
		}
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
