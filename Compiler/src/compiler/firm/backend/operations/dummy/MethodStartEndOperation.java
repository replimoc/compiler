package compiler.firm.backend.operations.dummy;

import java.util.HashSet;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

public abstract class MethodStartEndOperation extends AssemblerOperation {

	protected final CallingConvention callingConvention;
	protected int stackOperationSize;
	private HashSet<RegisterBundle> usedRegisters;

	public MethodStartEndOperation(CallingConvention callingConvention) {
		this.callingConvention = callingConvention;
	}

	@Override
	public abstract String[] toStringWithSpillcode();

	@Override
	public abstract String getComment();

	@Override
	public String getOperationString() {
		return "";
	}

	public void setStackOperationSize(int stackOperationSize) {
		this.stackOperationSize = stackOperationSize;
	}

	public void setUsedRegisters(HashSet<RegisterBundle> usedRegisters) {
		this.usedRegisters = usedRegisters;
	}

	protected final boolean isRegisterSaveNeeded(RegisterBundle registerBundle) {
		return usedRegisters.contains(registerBundle);
	}
}
