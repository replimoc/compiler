package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class SingleRegister extends RegisterBased {
	private static final byte FULL = 0b11;
	private static final byte HIGH = FULL;// 0b10;
	private static final byte LOW = FULL;// 0b01;

	public static final byte BLOCKED_REGISTER = 0b1111111;

	// reserved for special usage
	public static final SingleRegister RSP = new SingleRegister(Bit.BIT64, "%rsp", FULL); // stack pointer
	public static final SingleRegister RBP = new SingleRegister(Bit.BIT64, "%rbp", FULL); // frame pointer

	// registers with no 8 bit registers
	public static final SingleRegister RDI = new SingleRegister(Bit.BIT64, "%rdi", FULL);
	public static final SingleRegister EDI = new SingleRegister(Bit.BIT32, "%edi", FULL);

	public static final SingleRegister RSI = new SingleRegister(Bit.BIT64, "%rsi", FULL);
	public static final SingleRegister ESI = new SingleRegister(Bit.BIT32, "%esi", FULL);

	// registers with two 8bit registers
	public static final SingleRegister RAX = new SingleRegister(Bit.BIT64, "%rax", FULL); // accumulator
	public static final SingleRegister EAX = new SingleRegister(Bit.BIT32, "%eax", FULL); // accumulator
	public static final SingleRegister AH = new SingleRegister(Bit.BIT8, "%ah", HIGH); // accumulator
	public static final SingleRegister AL = new SingleRegister(Bit.BIT8, "%al", LOW); // accumulator

	public static final SingleRegister RBX = new SingleRegister(Bit.BIT64, "%rbx", FULL);
	public static final SingleRegister EBX = new SingleRegister(Bit.BIT32, "%ebx", FULL);
	public static final SingleRegister BH = new SingleRegister(Bit.BIT8, "%bh", HIGH);
	public static final SingleRegister BL = new SingleRegister(Bit.BIT8, "%bl", LOW);

	public static final SingleRegister RCX = new SingleRegister(Bit.BIT64, "%rcx", FULL);
	public static final SingleRegister ECX = new SingleRegister(Bit.BIT32, "%ecx", FULL);
	public static final SingleRegister CH = new SingleRegister(Bit.BIT8, "%ch", HIGH);
	public static final SingleRegister CL = new SingleRegister(Bit.BIT8, "%cl", LOW);

	public static final SingleRegister RDX = new SingleRegister(Bit.BIT64, "%rdx", FULL);
	public static final SingleRegister EDX = new SingleRegister(Bit.BIT32, "%edx", FULL);
	public static final SingleRegister DH = new SingleRegister(Bit.BIT8, "%dh", HIGH);
	public static final SingleRegister DL = new SingleRegister(Bit.BIT8, "%dl", LOW);

	// registers with one 8bit register
	public static final SingleRegister R8 = new SingleRegister(Bit.BIT64, "%r8", FULL);
	public static final SingleRegister R8D = new SingleRegister(Bit.BIT32, "%r8d", FULL);
	public static final SingleRegister R8B = new SingleRegister(Bit.BIT8, "%r8b", FULL);

	public static final SingleRegister R9 = new SingleRegister(Bit.BIT64, "%r9", FULL);
	public static final SingleRegister R9D = new SingleRegister(Bit.BIT32, "%r9d", FULL);
	public static final SingleRegister R9B = new SingleRegister(Bit.BIT8, "%r9b", FULL);

	public static final SingleRegister R10 = new SingleRegister(Bit.BIT64, "%r10", FULL);
	public static final SingleRegister R10D = new SingleRegister(Bit.BIT32, "%r10d", FULL);
	public static final SingleRegister R10B = new SingleRegister(Bit.BIT8, "%r10b", FULL);

	public static final SingleRegister R11 = new SingleRegister(Bit.BIT64, "%r11", FULL);
	public static final SingleRegister R11D = new SingleRegister(Bit.BIT32, "%r11d", FULL);
	public static final SingleRegister R11B = new SingleRegister(Bit.BIT8, "%r11b", FULL);

	public static final SingleRegister R12 = new SingleRegister(Bit.BIT64, "%r12", FULL);
	public static final SingleRegister R12D = new SingleRegister(Bit.BIT32, "%r12d", FULL);
	public static final SingleRegister R12B = new SingleRegister(Bit.BIT8, "%r12b", FULL);

	public static final SingleRegister R13 = new SingleRegister(Bit.BIT64, "%r13", FULL);
	public static final SingleRegister R13D = new SingleRegister(Bit.BIT32, "%r13d", FULL);
	public static final SingleRegister R13B = new SingleRegister(Bit.BIT8, "%r13b", FULL);

	public static final SingleRegister R14 = new SingleRegister(Bit.BIT64, "%r14", FULL);
	public static final SingleRegister R14D = new SingleRegister(Bit.BIT32, "%r14d", FULL);
	public static final SingleRegister R14B = new SingleRegister(Bit.BIT8, "%r14b", FULL);

	public static final SingleRegister R15 = new SingleRegister(Bit.BIT64, "%r15", FULL);
	public static final SingleRegister R15D = new SingleRegister(Bit.BIT32, "%r15d", FULL);
	public static final SingleRegister R15B = new SingleRegister(Bit.BIT8, "%r15b", FULL);

	private final String registerName;
	private final Bit mode;
	private final byte mask;
	private RegisterBundle registerBundle;

	private SingleRegister(Bit mode, String registerName, byte mask) {
		this.mode = mode;
		this.registerName = registerName;
		this.mask = mask;
	}

	public String getRegisterName() {
		return registerName;
	}

	@Override
	public String toString() {
		return registerName;
	}

	@Override
	public Bit getMode() {
		return mode;
	}

	public RegisterBundle getRegisterBundle() {
		return registerBundle;
	}

	void setRegisterBundle(RegisterBundle registerBundle) {
		this.registerBundle = registerBundle;
	}

	public byte getMask() {
		return mask;
	}
}
