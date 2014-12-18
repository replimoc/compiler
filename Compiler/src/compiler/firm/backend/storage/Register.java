package compiler.firm.backend.storage;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class Register extends Storage {
	public static final Register RAX = new Register("%rax"); // accumulator
	public static final Register RBX = new Register("%rbx");
	public static final Register RSP = new Register("%rsp"); // stack pointer
	public static final Register RBP = new Register("%rbp"); // frame pointer

	// 32-bit registers
	public static final Register EDI = new Register("%edi");
	public static final Register ESI = new Register("%esi");
	public static final Register EAX = new Register("%eax"); // gcc uses eax and edx for arithmetic operations
	public static final Register EDX = new Register("%edx");
	public static final Register ECX = new Register("%ecx"); // counter (for loop counters);

	public static final Register R8D = new Register("%r8d");
	public static final Register R9D = new Register("%r9d");

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
