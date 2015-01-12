package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class Register extends RegisterBased {
	// reserved for special usage
	public static final Register _SP = new Register(SingleRegister.RSP); // stack pointer
	public static final Register _BP = new Register(SingleRegister.RBP); // frame pointer

	// free registers
	public static final Register _DI = new Register(SingleRegister.RDI, SingleRegister.EDI);
	public static final Register _SI = new Register(SingleRegister.RSI, SingleRegister.ESI);

	// registers with 8bit regs
	public static final Register _AX = new Register(SingleRegister.RAX, SingleRegister.EAX, SingleRegister.AL); // accumulator
	public static final Register _BX = new Register(SingleRegister.RBX, SingleRegister.EBX, SingleRegister.BL);
	public static final Register _CX = new Register(SingleRegister.RCX, SingleRegister.ECX, SingleRegister.CL);
	public static final Register _DX = new Register(SingleRegister.RDX, SingleRegister.EDX, SingleRegister.DL);

	public static final Register _8D = new Register(SingleRegister.R8, SingleRegister.R8D, SingleRegister.R8B);
	public static final Register _9D = new Register(SingleRegister.R9, SingleRegister.R9D, SingleRegister.R9B);
	public static final Register _10D = new Register(SingleRegister.R10, SingleRegister.R10D, SingleRegister.R10B);
	public static final Register _11D = new Register(SingleRegister.R11, SingleRegister.R11D, SingleRegister.R11B);
	public static final Register _12D = new Register(SingleRegister.R12, SingleRegister.R12D, SingleRegister.R12B);
	public static final Register _13D = new Register(SingleRegister.R13, SingleRegister.R13D, SingleRegister.R13B);
	public static final Register _14D = new Register(SingleRegister.R14, SingleRegister.R14D, SingleRegister.R14B);
	public static final Register _15D = new Register(SingleRegister.R15, SingleRegister.R15D, SingleRegister.R15B);

	private final SingleRegister[] registers;

	Register(SingleRegister... registers) {
		this.registers = registers;
	}

	@Override
	public String toString(Bit bit) {
		return registers[bit.ordinal()].toString();
	}

	@Override
	public String toString() {
		return toString(Bit.BIT32);
	}
}
