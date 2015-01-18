package compiler.firm.backend.operations.cmov;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;

public class CmovSignOperation extends AssemblerBitOperation {

	private RegisterBased source;
	private RegisterBased destination;

	public CmovSignOperation(String comment, RegisterBased source, RegisterBased destination)
	{
		super(comment);
		this.source = source;
		this.destination = destination;
	}

	@Override
	public String getOperationString() {
		return String.format("\tcmovs %s, %s", source, destination);
	}
}