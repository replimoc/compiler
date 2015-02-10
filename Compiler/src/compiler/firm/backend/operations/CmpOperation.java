package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmpOperation extends SourceSourceOperation {

	public CmpOperation(String comment, Storage source, RegisterBased source2) {
		super(comment, source, source2);
	}

	@Override
	public String[] toStringWithSpillcode() {
		if (!source1.isSpilled() && source2.isSpilled()) {
			return new String[] { toString() };
		}
		return super.toStringWithSpillcode();
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmp%s %s, %s", source2.getMode(), source1.toString(), source2.toString());
	}

}
