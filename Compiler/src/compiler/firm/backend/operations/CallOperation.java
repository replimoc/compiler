package compiler.firm.backend.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import compiler.firm.backend.Bit;
import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.Register;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public class CallOperation extends AssemblerOperation {

	private static final int STACK_ITEM_SIZE = 8;

	private String name;
	private final List<Parameter> parameters;
	private final CallingConvention callingConvention;
	private List<Register> usedRegisters = new LinkedList<>();

	public CallOperation(String name, List<Parameter> parameters, CallingConvention callingConvention) {
		this.name = name;
		this.parameters = parameters;
		this.callingConvention = callingConvention;
		this.usedRegisters.add(Register._SP);
		this.usedRegisters.add(Register._BP);
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

	private List<Register> getSaveRegisters() {
		List<Register> saveRegisters = new ArrayList<>();
		for (Register register : callingConvention.callerSavedRegisters()) {
			if (this.usedRegisters.contains(register)) {
				saveRegisters.add(register);
			}
		}
		return saveRegisters;
	}

	@Override
	public String[] toStringWithSpillcode() {
		List<String> result = new LinkedList<String>();

		List<Register> callerSavedRegisters = getSaveRegisters();

		// Save all callerSavedRegisters to stack
		for (Register saveRegister : callerSavedRegisters) {
			result.add(new PushOperation(Bit.BIT64, saveRegister).toString());
		}

		// The following is before filling registers to have no problem with register allocation.
		Register[] callingRegisters = callingConvention.getParameterRegisters();
		int numberOfstackParameters = parameters.size() - callingRegisters.length;
		Constant stackAllocationSize = new Constant(STACK_ITEM_SIZE * numberOfstackParameters);

		if (numberOfstackParameters > 0) {
			result.add(new SubOperation(Bit.BIT64, stackAllocationSize, Register._SP).toString());

			Register temporaryRegister = getTemporaryRegister();

			// Copy parameters to stack
			for (int i = 0; i < numberOfstackParameters; i++) {
				Parameter source = parameters.get(i + callingRegisters.length);
				// Copy parameter
				MemoryPointer destinationPointer = new MemoryPointer(i * STACK_ITEM_SIZE, Register._SP);
				if (source.storage.getClass() == MemoryPointer.class
						|| (source.storage.getClass() == VirtualRegister.class && source.storage.isSpilled())) {
					result.add(new MovOperation(source.mode, source.storage, temporaryRegister).toString());
					result.add(new MovOperation(source.mode, temporaryRegister, destinationPointer).toString());
				} else {
					result.add(new MovOperation(source.mode, source.storage, destinationPointer).toString());
				}
			}
		}

		// Copy parameters in calling registers
		for (int i = 0; i < parameters.size() && i < callingRegisters.length; i++) {
			Parameter source = parameters.get(i);
			Register register = callingRegisters[i];
			if (register.toString(source.mode) == null) {
				result.add(new MovOperation(Bit.BIT64, source.storage, register).toString());
				result.add(new AndOperation(Bit.BIT64, new Constant(0xFF), register).toString()); // TODO: the mask should be saved in the mode
			} else {
				result.add(new MovOperation(source.mode, source.storage, register).toString());
			}
		}

		result.add(toString());

		if (numberOfstackParameters > 0) {
			result.add(new AddOperation(Bit.BIT64, stackAllocationSize, Register._SP).toString());
		}

		for (int i = callerSavedRegisters.size() - 1; i >= 0; i--) {
			result.add(new PopOperation(Bit.BIT64, callerSavedRegisters.get(i)).toString());
		}

		String[] resultStrings = new String[result.size()];
		result.toArray(resultStrings);
		return resultStrings;
	}

	public void addUsedRegisters(List<VirtualRegister> registers) {
		for (VirtualRegister registerBased : registers) {
			Storage storage = registerBased.getRegister();
			if (!(storage instanceof Register)) {
				continue;
			}
			this.usedRegisters.add((Register) storage);
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
