package compiler.firm.backend.operations.templates;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.utils.Utils;

public abstract class SourceOperation extends AssemblerBitOperation {

	protected RegisterBased source;

	public SourceOperation(RegisterBased source) {
		this(null, source);
	}

	public SourceOperation(String comment, RegisterBased source) {
		super(comment);
		this.source = source;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(this.source);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}
}
