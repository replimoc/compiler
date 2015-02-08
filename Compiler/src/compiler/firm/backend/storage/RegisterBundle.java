package compiler.firm.backend.storage;

import compiler.firm.backend.Bit;

// Map for registers: https://upload.wikimedia.org/wikipedia/commons/4/41/Table_of_x86_Registers.png
public class RegisterBundle {
	public static int REGISTER_COUNTER = 0;

	// reserved for special usage
	public static final RegisterBundle _SP = new RegisterBundle(SingleRegister.RSP); // stack pointer

	// free registers
	public static final RegisterBundle _DI = new RegisterBundle(SingleRegister.RDI, SingleRegister.EDI, SingleRegister.DIL);
	public static final RegisterBundle _SI = new RegisterBundle(SingleRegister.RSI, SingleRegister.ESI, SingleRegister.SIL);
	public static final RegisterBundle _BP = new RegisterBundle(SingleRegister.RBP, SingleRegister.EBP, SingleRegister.BPL);

	// registers with two 8bit regs
	public static final RegisterBundle _AX = new RegisterBundle(SingleRegister.RAX, SingleRegister.EAX, SingleRegister.AH, SingleRegister.AL); // accumulator
	public static final RegisterBundle _BX = new RegisterBundle(SingleRegister.RBX, SingleRegister.EBX, SingleRegister.BH, SingleRegister.BL);
	public static final RegisterBundle _CX = new RegisterBundle(SingleRegister.RCX, SingleRegister.ECX, SingleRegister.CH, SingleRegister.CL);
	public static final RegisterBundle _DX = new RegisterBundle(SingleRegister.RDX, SingleRegister.EDX, SingleRegister.DH, SingleRegister.DL);

	public static final RegisterBundle _8D = new RegisterBundle(SingleRegister.R8, SingleRegister.R8D, SingleRegister.R8B);
	public static final RegisterBundle _9D = new RegisterBundle(SingleRegister.R9, SingleRegister.R9D, SingleRegister.R9B);
	public static final RegisterBundle _10D = new RegisterBundle(SingleRegister.R10, SingleRegister.R10D, SingleRegister.R10B);
	public static final RegisterBundle _11D = new RegisterBundle(SingleRegister.R11, SingleRegister.R11D, SingleRegister.R11B);
	public static final RegisterBundle _12D = new RegisterBundle(SingleRegister.R12, SingleRegister.R12D, SingleRegister.R12B);
	public static final RegisterBundle _13D = new RegisterBundle(SingleRegister.R13, SingleRegister.R13D, SingleRegister.R13B);
	public static final RegisterBundle _14D = new RegisterBundle(SingleRegister.R14, SingleRegister.R14D, SingleRegister.R14B);
	public static final RegisterBundle _15D = new RegisterBundle(SingleRegister.R15, SingleRegister.R15D, SingleRegister.R15B);

	private final SingleRegister[][] registers = new SingleRegister[Bit.values().length][];
	private final int registerId;

	private RegisterBundle(SingleRegister register64) {
		this.registerId = REGISTER_COUNTER++;
		setModeRegisters(Bit.BIT64, register64);
	}

	private RegisterBundle(SingleRegister register64, SingleRegister register32) {
		this(register64);
		setModeRegisters(Bit.BIT32, register32);
	}

	private RegisterBundle(SingleRegister register64, SingleRegister register32, SingleRegister register8) {
		this(register64, register32);
		setModeRegisters(Bit.BIT8, register8);
	}

	private RegisterBundle(SingleRegister register64, SingleRegister register32, SingleRegister register8h, SingleRegister register8l) {
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
	public String toString() {
		return "RegisterBundle: " + registerId;
	}

	public int getRegisterId() {
		return registerId;
	}

	public SingleRegister getRegister(Bit mode) {
		SingleRegister[] modeRegisters = registers[mode.ordinal()];
		return modeRegisters != null ? modeRegisters[0] : null;
	}
}
