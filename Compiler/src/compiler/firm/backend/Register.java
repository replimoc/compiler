package compiler.firm.backend;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public enum Register {
	RAX("%rax"), // accumulator
	RSP("%rsp"), // stack pointer

	// 32-bit registers
	EDI("%edi"),
	ESI("%esi"),
	EDX("%edx"),
	ECX("%ecx"), ;

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
