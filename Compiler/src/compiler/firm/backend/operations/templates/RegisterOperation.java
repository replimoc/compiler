package compiler.firm.backend.operations.templates;

import java.util.Set;

import compiler.firm.backend.storage.RegisterBased;
import compiler.utils.Utils;

public abstract class RegisterOperation extends AssemblerBitOperation {

	protected RegisterBased register;

	public RegisterOperation(RegisterBased register) {
		this(null, register);
	}

	public RegisterOperation(String comment, RegisterBased register) {
		super(comment);
		this.register = register;
	}

	public RegisterBased getRegister() {
		return register;
	}

	@Override
	public Set<RegisterBased> getReadRegisters() {
		return Utils.unionSet(this.register);
	}

	@Override
	public Set<RegisterBased> getWriteRegisters() {
		return Utils.unionSet(this.register);
	}
}
