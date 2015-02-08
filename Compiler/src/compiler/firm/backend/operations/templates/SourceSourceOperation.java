package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Utils;

public abstract class SourceSourceOperation extends AssemblerBitOperation {

	protected final Storage source1;
	protected final RegisterBased source2;

	public SourceSourceOperation(String comment, Storage source1, RegisterBased source2) {
		super(comment);
		this.source1 = source1;
		this.source2 = source2;
	}

	public Storage getSource1() {
		return source1;
	}

	public RegisterBased getSource2() {
		return source2;
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.<RegisterBased> unionSet(source1.getReadRegisters(), source2.getReadRegisters());
	}

}
