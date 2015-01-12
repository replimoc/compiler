package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class Register extends RegisterBased {
	public static int REGISTER_COUNTER = 0;

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

	private final SingleRegister[][] registers = new SingleRegister[Bit.values().length][];
	private final int registerId;

	private Register(SingleRegister register64) {
		this.registerId = REGISTER_COUNTER++;
		setModeRegisters(Bit.BIT64, register64);
	}

	private Register(SingleRegister register64, SingleRegister register32) {
		this(register64);
		setModeRegisters(Bit.BIT32, register32);
	}

	private Register(SingleRegister register64, SingleRegister register32, SingleRegister register8) {
		this(register64, register32);
		setModeRegisters(Bit.BIT8, register8);
	}

	private Register(SingleRegister register64, SingleRegister register32, SingleRegister register8h, SingleRegister register8l) {
		this(register64, register32);
		setModeRegisters(Bit.BIT8, register8l, register8h);
	}

	private void setModeRegisters(Bit mode, SingleRegister... registers) {
		for (SingleRegister register : registers) {
			register.setRegisterBundle(this);
		}
		this.registers[mode.ordinal()] = registers;
	}

	@Override
	public String toString(Bit bit) {
		return registers[bit.ordinal()][0].toString();
	}

	@Override
	public String toString() {
		return toString(Bit.BIT32);
	}

	public int getRegisterId() {
		return registerId;
	}
}
