package compiler.firm.backend.operations;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.operations.templates.SourceSourceOperation;
import compiler.firm.backend.storage.RegisterBased;

public class TestOperation extends SourceSourceOperation {

	public TestOperation(String comment, RegisterBased source, RegisterBased destination) {
		super(comment, source, destination);
	}

	@Override
	public String getOperationString() {
		return String.format("\ttest %s, %s", source1.toString(), source2.toString());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Collections.emptySet();
	}
}
