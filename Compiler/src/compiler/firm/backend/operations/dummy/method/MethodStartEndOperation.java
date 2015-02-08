package compiler.firm.backend.operations.dummy.method;

import java.util.HashSet;
import java.util.Set;

import compiler.firm.backend.calling.CallingConvention;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBundle;

public abstract class MethodStartEndOperation extends AssemblerOperation {

	protected final CallingConvention callingConvention;
	private final int stackItemSize;
	protected final boolean isMain;
	protected int stackOperationSize;
	private Set<RegisterBundle> usedRegisters = new HashSet<>();

	public MethodStartEndOperation(String comment, CallingConvention callingConvention, int stackItemSize, boolean isMain) {
		super(comment);
		this.callingConvention = callingConvention;
		this.stackItemSize = stackItemSize;
		this.isMain = isMain;
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
