package compiler.firm.backend.registerallocation;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.SingleRegister;

public final class RegisterAllocationPolicy {

	public static final RegisterAllocationPolicy ALL_A_B_C_D_8_9_10_11_12_DI_SI = new RegisterAllocationPolicy(new SingleRegister[][] {
			// 64bit registers
			{ SingleRegister.RAX, SingleRegister.RBX, SingleRegister.RCX, SingleRegister.RDX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11, SingleRegister.R12,
					SingleRegister.RDI, SingleRegister.RSI },
			// 32 bit registers
			{ SingleRegister.EAX, SingleRegister.EBX, SingleRegister.ECX, SingleRegister.EDX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D, SingleRegister.R12D,
					SingleRegister.EDI, SingleRegister.ESI },
			// 8 bit registers
			{ SingleRegister.AL, SingleRegister.BL, SingleRegister.CL, SingleRegister.DL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B, SingleRegister.R12B,
					SingleRegister.DIL, SingleRegister.SIL }
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
}
