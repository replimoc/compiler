package compiler.firm.backend.operations.dummy;

import java.util.Collections;
import java.util.Set;

import compiler.firm.backend.operations.templates.AssemblerBitOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class PhiOperation extends AssemblerBitOperation {

	private final VirtualRegister register;

	public PhiOperation(String comment, VirtualRegister register) {
		super(comment);

		this.register = register;
	}

	@Override
	public String getOperationString() {
		return "\t# phi: " + register.toString();
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
		return Utils.unionSet(register.getUsedRegister());
	}

	public VirtualRegister getRegister() {
		return register;
	}
}
