package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class StorageRegisterRegisterOperation extends SourceSourceDestinationOperation {

	public StorageRegisterRegisterOperation(String comment, Storage storage, RegisterBased source2, RegisterBased destination) {
		super(comment, storage, source2, destination);
	}

	@Override
	public RegisterBased getSource2() {
		return (RegisterBased) source2;
	}

	@Override
	public RegisterBased getDestination() {
		return (RegisterBased) super.getDestination();
	}
}
