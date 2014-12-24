package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class Register extends Storage {
	public static final Register RBX = new Register("%ebx", "%rbx");
	public static final Register RSP = new Register("%esp", "%rsp"); // stack pointer
	public static final Register RBP = new Register("%ebp", "%rbp"); // frame pointer

	// 32-bit registers
	public static final Register EDI = new Register("%edi", "%rdi");
	public static final Register ESI = new Register("%esi", "%rsi");
	public static final Register EAX = new Register("%eax", "%rax"); // accumulator
	public static final Register RAX = EAX; // accumulator
	public static final Register EDX = new Register("%edx", "%rdx");
	public static final Register ECX = new Register("%ecx", "%rcx"); // counter (for loop counters);

	public static final Register R8D = new Register("%r8d", "%r8");
	public static final Register R9D = new Register("%r9d", "%r9");

	private final String registerName32;
	private final String registerName64;

	Register(String registerName32, String registerName64) {
		this.registerName32 = registerName32;
		this.registerName64 = registerName64;
	}

	public String getRegisterName() {
		return registerName32;
	}

	@Override
	public String toString(Bit bit) {
		switch (bit) {
		case BIT64:
			return registerName64;
		case BIT32:
		default:
			return registerName32;

		}
	}

	@Override
	public String toString() {
		return toString(Bit.BIT32);
	}
}
