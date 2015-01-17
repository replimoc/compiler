package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.StorageRegisterOperation;
import compiler.firm.backend.storage.RegisterBased;

public class TestOperation extends StorageRegisterOperation {

	public TestOperation(String comment, RegisterBased source, RegisterBased destination) {
		super(comment, source, destination);
	}

    @Override
    public String getOperationString() {
        return String.format("\ttest %s, %s", getStorage().toString(), getDestination().toString());
    }
}
