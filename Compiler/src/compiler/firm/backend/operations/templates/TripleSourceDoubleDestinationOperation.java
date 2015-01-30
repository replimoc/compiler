package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public abstract class TripleSourceDoubleDestinationOperation extends DoubleSourceDoubleDestinationOperation {

	protected final RegisterBased source3;

	public TripleSourceDoubleDestinationOperation(String comment, RegisterBased source1, RegisterBased source2, RegisterBased source3,
			VirtualRegister destination1, VirtualRegister destination2) {
		super(comment, source1, source2, destination1, destination2);
		this.source3 = source3;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.<RegisterBased> unionSet(source1.getReadRegisters(), source2.getReadRegisters(), source3.getReadRegisters());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.<RegisterBased> unionSet(destination1, destination2);
	}
}
