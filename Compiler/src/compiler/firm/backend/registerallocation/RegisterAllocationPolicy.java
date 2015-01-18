package compiler.firm.backend.registerallocation;

import java.util.Arrays;
import java.util.LinkedList;

import compiler.firm.backend.storage.SingleRegister;

public final class RegisterAllocationPolicy {

	public static final RegisterAllocationPolicy ALL_A_B_C_D_8_9_10_11_12_DI_SI = new RegisterAllocationPolicy(
			// 64bit registers
			getList(SingleRegister.RAX, SingleRegister.RBX, SingleRegister.RCX, SingleRegister.RDX,
					SingleRegister.R8, SingleRegister.R9, SingleRegister.R10, SingleRegister.R11, SingleRegister.R12,
					SingleRegister.RDI, SingleRegister.RSI),
			// 32 bit registers
			getList(SingleRegister.EAX, SingleRegister.EBX, SingleRegister.ECX, SingleRegister.EDX,
					SingleRegister.R8D, SingleRegister.R9D, SingleRegister.R10D, SingleRegister.R11D, SingleRegister.R12D,
					SingleRegister.EDI, SingleRegister.ESI),
			// 8 bit registers
			getList(SingleRegister.AL, SingleRegister.BL, SingleRegister.CL, SingleRegister.DL,
					SingleRegister.R8B, SingleRegister.R9B, SingleRegister.R10B, SingleRegister.R11B, SingleRegister.R12B,
					SingleRegister.DIL, SingleRegister.SIL)
			);

	public static final RegisterAllocationPolicy NO_REGISTERS = new RegisterAllocationPolicy(
			// 64bit registers
			getList(),
			// 32 bit registers
			getList(),
			// 8 bit registers
			getList()
			);

	private final LinkedList<SingleRegister>[] allowedRegisters;

	@SafeVarargs
	private RegisterAllocationPolicy(LinkedList<SingleRegister>... allowedRegisters) {
		this.allowedRegisters = allowedRegisters;
	}

	public LinkedList<SingleRegister>[] getAllowedRegisters() {
		return allowedRegisters;
	}

	private static LinkedList<SingleRegister> getList(SingleRegister... registers) {
		return new LinkedList<>(Arrays.asList(registers));
	}
}
