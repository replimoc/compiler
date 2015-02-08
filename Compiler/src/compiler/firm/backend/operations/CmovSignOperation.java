package compiler.firm.backend.operations;

import compiler.firm.backend.operations.templates.SourceSourceDestinationOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public class CmovSignOperation extends SourceSourceDestinationOperation {
	private boolean swapped = false;

	public CmovSignOperation(String comment, Storage source1, RegisterBased source2, RegisterBased destination) {
		super(comment, source1, source2, destination);
	}

	@Override
	public String getOperationString() {
		String negate = "";
		if (swapped) {
			negate = "n";
		}
		return String.format("\tcmov%ss %s, %s", negate, source, destination);
	}

	@Override
	protected void swapSources() {
		swapped = true;
		super.swapSources();
	}
}