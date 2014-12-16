package compiler.firm.backend;

public enum Register {
	RAX("%rax"), // accumulator
	;

	private final String registerName;

	Register(String registerName) {
		this.registerName = registerName;
	}

	public String getRegisterName() {
		return registerName;
	}

	@Override
	public String toString() {
		return getRegisterName();
	}
}
