package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class Register extends RegisterBased {
	// reserved for special usage
	public static final Register _SP = new Register(null, "%esp", "%rsp"); // stack pointer
	public static final Register _BP = new Register(null, "%ebp", "%rbp"); // frame pointer

	// free registers
	public static final Register _DI = new Register(null, "%edi", "%rdi");
	public static final Register _SI = new Register(null, "%esi", "%rsi");

	// registers with 8bit regs
	public static final Register _AX = new Register("%al", "%eax", "%rax"); // accumulator
	public static final Register _BX = new Register("%bl", "%ebx", "%rbx");
	public static final Register _CX = new Register("%cl", "%ecx", "%rcx"); // counter (for loop counters);
	public static final Register _DX = new Register("%dl", "%edx", "%rdx");
	public static final Register _8D = new Register("%r8b", "%r8d", "%r8");
	public static final Register _9D = new Register("%r9b", "%r9d", "%r9");
	public static final Register _10D = new Register("%r10b", "%r10d", "%r10");
	public static final Register _11D = new Register("%r11b", "%r11d", "%r11");
	public static final Register _12D = new Register("%r12b", "%r12d", "%r12");
	public static final Register _13D = new Register("%r13b", "%r13d", "%r13");
	public static final Register _14D = new Register("%r14b", "%r14d", "%r14");
	public static final Register _15D = new Register("%r15b", "%r15d", "%r15");

	private final String registerName8;
	private final String registerName32;
	private final String registerName64;

	Register(String registerName8, String registerName32, String registerName64) {
		this.registerName8 = registerName8;
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
		case BIT8:
			return registerName8;
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
