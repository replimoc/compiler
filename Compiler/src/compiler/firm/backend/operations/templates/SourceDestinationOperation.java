package compiler.firm.backend.operations.templates;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class SourceDestinationOperation extends AssemblerBitOperation {

	private final Storage source;
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
		RegisterBased sourceRegister = source.getUsedRegister();
		if (sourceRegister != null) {
			return new RegisterBased[] { sourceRegister };
		} else {
			return new RegisterBased[] {};
		}
	}

	@Override
	public RegisterBased[] getUsedRegisters() {
		RegisterBased sourceRegister = source.getUsedRegister();
		RegisterBased destinationRegister = destination.getUsedRegister();
		if (sourceRegister != null && destinationRegister != null) {
			return new RegisterBased[] { sourceRegister, destinationRegister };
		} else if (sourceRegister != null) {
			return new RegisterBased[] { sourceRegister };
		} else if (destinationRegister != null) {
			return new RegisterBased[] { destinationRegister };
		} else {
			return new RegisterBased[] {};
		}
	}
}
