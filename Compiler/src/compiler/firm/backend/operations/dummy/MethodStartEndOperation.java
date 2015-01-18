package compiler.firm.backend.operations.dummy;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;

public abstract class MethodStartEndOperation extends AssemblerOperation {

	protected final CallingConvention callingConvention;
	protected boolean isMain;
	protected int stackOperationSize;

	public MethodStartEndOperation(CallingConvention callingConvention) {
		this.callingConvention = callingConvention;
	}

	@Override
	public abstract String[] toStringWithSpillcode();

	@Override
	public abstract String getComment();

	@Override
	public String getOperationString() {
		throw new RuntimeException("this should never be called.");
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}

	public void setStackOperationSize(int stackOperationSize) {
		this.stackOperationSize = stackOperationSize;
	}

}
