package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public abstract class SourceSourceOperation extends AssemblerBitOperation {

	protected final Storage source1;
	protected final RegisterBased source2;

	public SourceSourceOperation(String comment, Storage source1, RegisterBased source2) {
		super(comment);
		this.source1 = source1;
		this.source2 = source2;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

}
