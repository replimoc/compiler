package compiler.firm.backend.operations.templates;

import java.util.Arrays;

import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.firm.backend.storage.VirtualRegister;

public abstract class SourceDestinationOperation extends AssemblerBitOperation {

	protected Storage source;
	protected final Storage destination;

	public SourceDestinationOperation(String comment, Storage source, Storage destination) {
		super(comment);
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
			if ((source.getClass() == VirtualRegister.class || source.getClass() == Constant.class)
					&& (destination.getClass() == VirtualRegister.class || destination.getClass() == Constant.class)) {

				if ((source.isSpilled() && !destination.isSpilled()) || (!source.isSpilled() && destination.isSpilled())) {
					return new String[] { toString() };
				} else {
					Storage oldSource = this.source;
					this.source = getTemporaryRegister().getRegister(destination.getMode());
					String[] result = new String[] {
							new MovOperation(oldSource, this.source).toString(),
							toString()
					};
					this.source = oldSource;
					return result;
				}
			}
		}
		return super.toStringWithSpillcode();
	}
}
