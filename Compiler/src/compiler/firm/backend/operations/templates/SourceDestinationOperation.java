package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public abstract class SourceDestinationOperation extends AssemblerBitOperation {

	protected RegisterBased source;
	protected RegisterBased destination;

	public SourceDestinationOperation(RegisterBased source, RegisterBased destination) {
		super(null);
		this.source = source;
		this.destination = destination;

		if (source instanceof VirtualRegister && destination instanceof VirtualRegister) {
			((VirtualRegister) destination).addPreferedRegister((VirtualRegister) source);
		}
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(this.source);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(this.destination);
	}

	@Override
	protected MovOperation getPreOperation() {
		if (source.getSingleRegister() == null
				|| source.getSingleRegister() != destination.getSingleRegister()) {
			return new MovOperation(source, destination);
		}
		return null;
	}

}
