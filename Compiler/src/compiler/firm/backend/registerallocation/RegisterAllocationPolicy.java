package compiler.firm.backend.registerallocation;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.SingleRegister;

public final class RegisterAllocationPolicy {

	public static final RegisterAllocationPolicy A_B_C_D_8_12_13_14_15_BP_DI_SI = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{ SingleRegister.RAX, SingleRegister.RBX, SingleRegister.RCX, SingleRegister.RDX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11, SingleRegister.R12,
					SingleRegister.RBP, SingleRegister.RDI, SingleRegister.RSI,
					SingleRegister.R13, SingleRegister.R14, SingleRegister.R15 },
			// 32 bit registers
			{ SingleRegister.EAX, SingleRegister.EBX, SingleRegister.ECX, SingleRegister.EDX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D, SingleRegister.R12D,
					SingleRegister.EBP, SingleRegister.EDI, SingleRegister.ESI,
					SingleRegister.R13D, SingleRegister.R14D, SingleRegister.R15D },
			// 8 bit registers
			{ SingleRegister.AL, SingleRegister.BL, SingleRegister.CL, SingleRegister.DL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B, SingleRegister.R12B,
					SingleRegister.BPL, SingleRegister.DIL, SingleRegister.SIL,
					SingleRegister.R13B, SingleRegister.R14B, SingleRegister.R15B } });

	public static final RegisterAllocationPolicy A_B_C_D_8_9_10_11_12_13_14_15_BP_DI_SI = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{ SingleRegister.RAX, SingleRegister.RBX, SingleRegister.RCX, SingleRegister.RDX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11, SingleRegister.R12,
					SingleRegister.RBP, SingleRegister.RDI, SingleRegister.RSI,
					SingleRegister.R13, SingleRegister.R14, SingleRegister.R15 },
			// 32 bit registers
			{ SingleRegister.EAX, SingleRegister.EBX, SingleRegister.ECX, SingleRegister.EDX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D, SingleRegister.R12D,
					SingleRegister.EBP, SingleRegister.EDI, SingleRegister.ESI,
					SingleRegister.R13D, SingleRegister.R14D, SingleRegister.R15D },
			// 8 bit registers
			{ SingleRegister.AL, SingleRegister.BL, SingleRegister.CL, SingleRegister.DL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B, SingleRegister.R12B,
					SingleRegister.BPL, SingleRegister.DIL, SingleRegister.SIL,
					SingleRegister.R13B, SingleRegister.R14B, SingleRegister.R15B }
	});

	public static final RegisterAllocationPolicy A_BP_B_12_13_14_15__DI_SI_D_C_8 = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{ SingleRegister.RAX, SingleRegister.RBP, SingleRegister.RBX,
					SingleRegister.R12, SingleRegister.R13, SingleRegister.R14, SingleRegister.R15,
					SingleRegister.RDI, SingleRegister.RSI, SingleRegister.RDX, SingleRegister.RCX,
					SingleRegister.R8 },
			// 32 bit registers
			{ SingleRegister.EAX, SingleRegister.EBP, SingleRegister.EBX,
					SingleRegister.R12D, SingleRegister.R13D, SingleRegister.R14D, SingleRegister.R15D,
					SingleRegister.EDI, SingleRegister.ESI, SingleRegister.EDX, SingleRegister.ECX,
					SingleRegister.R8D },
			// 8 bit registers
			{ SingleRegister.AL, SingleRegister.BPL, SingleRegister.BL,
					SingleRegister.R12B, SingleRegister.R13B, SingleRegister.R14B, SingleRegister.R15B,
					SingleRegister.DIL, SingleRegister.SIL, SingleRegister.DL, SingleRegister.CL,
					SingleRegister.R8B }
	});

	public static final RegisterAllocationPolicy A_BP_B_12_13_14_15__DI_SI_D_C_8_9_10_11 = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{ SingleRegister.RAX, SingleRegister.RBP, SingleRegister.RBX,
					SingleRegister.R12, SingleRegister.R13, SingleRegister.R14, SingleRegister.R15,
					SingleRegister.RDI, SingleRegister.RSI, SingleRegister.RDX, SingleRegister.RCX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11 },
			// 32 bit registers
			{ SingleRegister.EAX, SingleRegister.EBP, SingleRegister.EBX,
					SingleRegister.R12D, SingleRegister.R13D, SingleRegister.R14D, SingleRegister.R15D,
					SingleRegister.EDI, SingleRegister.ESI, SingleRegister.EDX, SingleRegister.ECX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D },
			// 8 bit registers
			{ SingleRegister.AL, SingleRegister.BPL, SingleRegister.BL,
					SingleRegister.R12B, SingleRegister.R13B, SingleRegister.R14B, SingleRegister.R15B,
					SingleRegister.DIL, SingleRegister.SIL, SingleRegister.DL, SingleRegister.CL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B }
	});

	public static final RegisterAllocationPolicy NO_REGISTERS = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{},
			// 32 bit registers
			{},
			// 8 bit registers
			{}
	});

	private final SingleRegister[][] allowedRegisters;

	private RegisterAllocationPolicy(SingleRegister[][] allowedRegisters) {
		assert allowedRegisters.length == Bit.values().length;

		this.allowedRegisters = allowedRegisters;
	}

	public SingleRegister[][] getAllowedRegisters() {
		SingleRegister[][] result = new SingleRegister[allowedRegisters.length][];

		for (int i = 0; i < allowedRegisters.length; i++) {
			int length = allowedRegisters[i].length;
			result[i] = new SingleRegister[length];
			System.arraycopy(allowedRegisters[i], 0, result[i], 0, length);
		}

		return result;
	}

	public int getNumberOfRegisters(Bit mode) {
		return allowedRegisters[mode.ordinal()].length;
	}

	public SingleRegister[] getAllowedRegisters(Bit mode) {
		return allowedRegisters[mode.ordinal()];
	}
}
