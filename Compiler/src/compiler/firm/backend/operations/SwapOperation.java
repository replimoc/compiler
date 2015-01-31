package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.utils.Utils;

public class SwapOperation extends AssemblerBitOperation {

	private RegisterBased operand1;
	private RegisterBased operand2;

	public SwapOperation(String comment, RegisterBased operand1, RegisterBased operand2) {
		super(comment);
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	@Override
	public String getOperationString() {
		return String.format("\txchg %s, %s", operand1, operand2);
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(operand1.getReadRegisters(), operand2.getReadRegistersOnRightSide());
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(operand1.getWriteRegisters(), operand2.getWriteRegisters());
	}
}
