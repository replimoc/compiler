package compiler.firm.backend.storage;

public abstract class RegisterBased extends Storage {
	@Override
	public RegisterBased getUsedRegister() {
		return this;
	}
}
