package compiler.firm.backend.operations.dummy;

import java.util.Set;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

public abstract class MethodStartEndOperation extends AssemblerOperation {

	private int stackItemSize;
	protected final CallingConvention callingConvention;
	protected int stackOperationSize;
	private Set<RegisterBundle> usedRegisters;

	public MethodStartEndOperation(CallingConvention callingConvention, int stackItemSize) {
		this.callingConvention = callingConvention;
		this.stackItemSize = stackItemSize;
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

	public void setUsedRegisters(Set<RegisterBundle> usedRegisters) {
		this.usedRegisters = usedRegisters;
	}

	protected final boolean isRegisterSaveNeeded(RegisterBundle registerBundle) {
		return usedRegisters.contains(registerBundle);
	}

	public int getStackOffset() {
		int offset = 1;
		RegisterBundle[] registers = callingConvention.calleeSavedRegisters();
		for (int i = 0; i < registers.length; i++) {
			if (isRegisterSaveNeeded(registers[i])) {
				offset += 1;
			}
		}
		return offset * this.stackItemSize + stackOperationSize;
	}
}
