package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

public abstract class SourceSourceDestinationOperation extends AssemblerBitOperation {

	protected Storage source;
	protected Storage source2;
	protected final Storage destination;

	public SourceSourceDestinationOperation(String comment, Storage source, Storage source2, Storage destination) {
		super(comment);
		this.source = source;
		this.source2 = source2;
		this.destination = destination;
	}

	public Storage getSource() {
		return source;
	}

	public Storage getSource2() {
		return source2;
	}

	public Storage getDestination() {
		return destination;
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

}
