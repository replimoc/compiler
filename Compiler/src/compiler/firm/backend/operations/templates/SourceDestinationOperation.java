package compiler.firm.backend.operations.templates;

import java.util.Arrays;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class SourceDestinationOperation extends AssemblerBitOperation {

	private Storage source;
	private final Storage destination;

	public SourceDestinationOperation(String comment, Bit mode, Storage source, Storage destination) {
		super(comment, mode);
		this.source = source;
		this.destination = destination;
	}

	public Storage getSource() {
		return source;
	}

	public Storage getDestination() {
		return destination;
	}

	@Override
	public RegisterBased[] getReadRegisters() {
		RegisterBased[] sourceRegister = source.getUsedRegister();
		RegisterBased[] destinationRegister = destination.getReadOnRightSideRegister();
		return calculateRegisters(sourceRegister, destinationRegister);
	}

	@Override
	public RegisterBased[] getWriteRegisters() {
		RegisterBased[] destinationRegister = destination.getUsedRegister();
		return calculateRegisters(null, destinationRegister);
	}

	private RegisterBased[] calculateRegisters(RegisterBased[] sourceRegister, RegisterBased[] destinationRegister) {
		if (sourceRegister != null && destinationRegister != null) {
			RegisterBased[] allRegisters = Arrays.copyOf(sourceRegister, sourceRegister.length + destinationRegister.length);
			System.arraycopy(destinationRegister, 0, allRegisters, sourceRegister.length, destinationRegister.length);
			return allRegisters;
		} else if (sourceRegister != null) {
			return sourceRegister;
		} else if (destinationRegister != null) {
			return destinationRegister;
		} else {
			return new RegisterBased[] {};
		}
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (hasSpilledRegisters()) {
			if ((getSource().getClass() == VirtualRegister.class || getSource().getClass() == Constant.class)
					&& (getDestination().getClass() == VirtualRegister.class || getDestination().getClass() == Constant.class)) {
				Storage source = getSource();
				Storage destination = getDestination();

				if ((source.isSpilled() && !destination.isSpilled()) ||
						(!source.isSpilled() && destination.isSpilled())) {
					return new String[] { toString() };
				} else {
					this.source = getTemporaryRegister();
					String[] result = new String[] {
							new MovOperation(getMode(), source, this.source).toString(),
							toString()
					};
					this.source = source;
					return result;
				}
			}
		}
		return super.toStringWithSpillcode();
	}
}
