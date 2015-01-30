package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public abstract class DoubleSourceDoubleDestinationOperation extends AssemblerBitOperation {

	protected final RegisterBased source1;
	protected final RegisterBased source2;
	protected final VirtualRegister destination1;
	protected final VirtualRegister destination2;

	public DoubleSourceDoubleDestinationOperation(String comment, RegisterBased source1, RegisterBased source2,
			VirtualRegister destination1, VirtualRegister destination2) {
		super(comment);
		this.source1 = source1;
		this.source2 = source2;
		this.destination1 = destination1;
		this.destination2 = destination2;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.<RegisterBased> unionSet(source1.getReadRegisters(), source2.getReadRegisters());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.<RegisterBased> unionSet(destination1, destination2);
	}
}
