package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class SingleRegister extends RegisterBased {
	// reserved for special usage
	public static final SingleRegister RSP = new SingleRegister(Bit.BIT64, "%rsp"); // stack pointer
	public static final SingleRegister RBP = new SingleRegister(Bit.BIT64, "%rbp"); // frame pointer

	// free registers
	public static final SingleRegister RDI = new SingleRegister(Bit.BIT64, "%rdi");
	public static final SingleRegister EDI = new SingleRegister(Bit.BIT32, "%edi");
	public static final SingleRegister RSI = new SingleRegister(Bit.BIT64, "%rsi");
	public static final SingleRegister ESI = new SingleRegister(Bit.BIT32, "%esi");

	// registers with 8bit regs
	public static final SingleRegister RAX = new SingleRegister(Bit.BIT64, "%rax"); // accumulator
	public static final SingleRegister EAX = new SingleRegister(Bit.BIT32, "%eax"); // accumulator
	public static final SingleRegister AH = new SingleRegister(Bit.BIT8, "%ah"); // accumulator
	public static final SingleRegister AL = new SingleRegister(Bit.BIT8, "%al"); // accumulator

	public static final SingleRegister RBX = new SingleRegister(Bit.BIT64, "%rbx");
	public static final SingleRegister EBX = new SingleRegister(Bit.BIT32, "%ebx");
	public static final SingleRegister BH = new SingleRegister(Bit.BIT8, "%bh");
	public static final SingleRegister BL = new SingleRegister(Bit.BIT8, "%bl");

	public static final SingleRegister RCX = new SingleRegister(Bit.BIT64, "%rcx");
	public static final SingleRegister ECX = new SingleRegister(Bit.BIT32, "%ecx");
	public static final SingleRegister CH = new SingleRegister(Bit.BIT8, "%ch");
	public static final SingleRegister CL = new SingleRegister(Bit.BIT8, "%cl");

	public static final SingleRegister RDX = new SingleRegister(Bit.BIT64, "%rdx");
	public static final SingleRegister EDX = new SingleRegister(Bit.BIT32, "%edx");
	public static final SingleRegister DH = new SingleRegister(Bit.BIT8, "%dh");
	public static final SingleRegister DL = new SingleRegister(Bit.BIT8, "%dl");

	public static final SingleRegister R8 = new SingleRegister(Bit.BIT64, "%r8");
	public static final SingleRegister R8D = new SingleRegister(Bit.BIT32, "%r8d");
	public static final SingleRegister R8B = new SingleRegister(Bit.BIT8, "%r8b");

	public static final SingleRegister R9 = new SingleRegister(Bit.BIT64, "%r9");
	public static final SingleRegister R9D = new SingleRegister(Bit.BIT32, "%r9d");
	public static final SingleRegister R9B = new SingleRegister(Bit.BIT8, "%r9b");

	public static final SingleRegister R10 = new SingleRegister(Bit.BIT64, "%r10");
	public static final SingleRegister R10D = new SingleRegister(Bit.BIT32, "%r10d");
	public static final SingleRegister R10B = new SingleRegister(Bit.BIT8, "%r10b");

	public static final SingleRegister R11 = new SingleRegister(Bit.BIT64, "%r11");
	public static final SingleRegister R11D = new SingleRegister(Bit.BIT32, "%r11d");
	public static final SingleRegister R11B = new SingleRegister(Bit.BIT8, "%r11b");

	public static final SingleRegister R12 = new SingleRegister(Bit.BIT64, "%r12");
	public static final SingleRegister R12D = new SingleRegister(Bit.BIT32, "%r12d");
	public static final SingleRegister R12B = new SingleRegister(Bit.BIT8, "%r12b");

	public static final SingleRegister R13 = new SingleRegister(Bit.BIT64, "%r13");
	public static final SingleRegister R13D = new SingleRegister(Bit.BIT32, "%r13d");
	public static final SingleRegister R13B = new SingleRegister(Bit.BIT8, "%r13b");

	public static final SingleRegister R14 = new SingleRegister(Bit.BIT64, "%r14");
	public static final SingleRegister R14D = new SingleRegister(Bit.BIT32, "%r14d");
	public static final SingleRegister R14B = new SingleRegister(Bit.BIT8, "%r14b");

	public static final SingleRegister R15 = new SingleRegister(Bit.BIT64, "%r15");
	public static final SingleRegister R15D = new SingleRegister(Bit.BIT32, "%r15d");
	public static final SingleRegister R15B = new SingleRegister(Bit.BIT8, "%r15b");

	private final String registerName;
	private final Bit mode;
	private Register registerBundle;

	private SingleRegister(Bit mode, String registerName) {
		this.mode = mode;
		this.registerName = registerName;
	}

	public String getRegisterName() {
		return registerName;
	}

	@Override
	public String toString(Bit mode) {
		if (this.mode != mode) {
			throw new RuntimeException("Used register with wrong mode: this is " + this.mode + " not " + mode);
		}
		return registerName;
	}

	@Override
	public String toString() {
		return registerName;
	}

	public Bit getMode() {
		return mode;
	}

	public Register getRegisterBundle() {
		return registerBundle;
	}

	void setRegisterBundle(Register registerBundle) {
		this.registerBundle = registerBundle;
	}
}
