package compiler.firm.backend.operations.dummy.phi;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;

public class PhiWriteOperation extends AssemblerBitOperation {

	private Set<RegisterBased> registers;

	public PhiWriteOperation(Set<RegisterBased> registers) {
		super(null);
		this.registers = registers;
	}

	@Override
	public String getOperationString() {
		return "\t# phi write";
	}

	@Override
	public String[] toStringWithSpillcode() {
		return new String[] { toString() };
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Collections.emptySet();
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return registers;
	}

}
