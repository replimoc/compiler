package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

public abstract class SourceSourceDestinationOperation extends AssemblerBitOperation {

	protected Storage source;
	protected RegisterBased source2;
	protected final RegisterBased destination;

	public SourceSourceDestinationOperation(String comment, Storage source, RegisterBased source2, RegisterBased destination) {
		super(comment);
		this.source = source;
		this.source2 = source2;
		this.destination = destination;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		RegisterBased[] sourceRegisters = source.getUsedRegister();
		RegisterBased[] source2Registers = source2.getUsedRegister();
		RegisterBased[] destinationRegisters = destination.getReadOnRightSideRegister();
		return Utils.unionSet(sourceRegisters, source2Registers, destinationRegisters);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(destination.getUsedRegister());
	}

	public Storage getSource() {
		return source;
	}

	public RegisterBased getSource2() {
		return source2;
	}

	public RegisterBased getDestination() {
		return destination;
	}

	@Override
	protected MovOperation getPreOperation() {
		// TODO: Detect equal StackPointers.
		if (source2.getSingleRegister() == null
				|| source2.getSingleRegister() != destination.getSingleRegister()) {
			return new MovOperation(source2, destination);

		}
		return null;
	}
}
