package compiler.firm.backend.operations;

import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Utils;

public class CltdOperation extends AssemblerOperation {

	public CltdOperation() {
		this(null);
	}

	public CltdOperation(String comment) {
		super(comment);
	}

	@Override
	public String getOperationString() {
		return String.format("\tcltd");
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.<RegisterBased> unionSet(new VirtualRegister(Bit.BIT64, SingleRegister.RAX));
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.<RegisterBased> unionSet(new VirtualRegister(Bit.BIT64, SingleRegister.RDX));
	}
}
