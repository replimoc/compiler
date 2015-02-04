package compiler.firm.backend.registerallocation.ssa;

import java.util.HashSet;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.operations.templates.CurrentlyAliveRegistersNeeding;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.nodes.Block;

public class SsaRegisterAllocator {
	private static boolean DEBUG = true;
	private final AssemblerProgram program;

	public SsaRegisterAllocator(AssemblerProgram program) {
		this.program = program;
	}

	public void colorGraph(final RegisterAllocationPolicy policy) {
		FirmUtils.walkDominanceTree(program.getStartBlock(), new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				colorBlock(block, policy);
			}
		});

		debugln("used registers (" + program.getUsedRegisters().size() + "): " + program.getUsedRegisters());
	}

	private void colorBlock(Block block, RegisterAllocationPolicy policy) {
		AssemblerOperationsBlock operationsBlock = program.getOperationsBlock(block);
		if (operationsBlock == null) {
			return;
		}

		Set<RegisterBundle> freeRegisters = calculateFreeRegistersAtStart(policy, operationsBlock);

		for (AssemblerOperation operation : operationsBlock.getOperations()) {
			for (VirtualRegister readRegister : operation.getVirtualReadRegisters()) {
				if (!readRegister.isSpilled() && operationsBlock.isLastUsage(readRegister, operation)) {
					if (policy.contains(readRegister.getRegisterBundle()))
						freeRegisters.add(readRegister.getRegisterBundle());
				}
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				if (writeRegister.getRegister() == null && !writeRegister.isSpilled()) {
					assignFreeRegister(policy, freeRegisters, writeRegister);
				}
			}

			checkForOperationAssignments(operation, policy, freeRegisters); // irrelevant for register allocation
		}
	}

	private static Set<RegisterBundle> calculateFreeRegistersAtStart(RegisterAllocationPolicy policy, AssemblerOperationsBlock operationsBlock) {
		Set<RegisterBundle> freeRegisters = policy.getAllowedBundles(Bit.BIT64);

		for (VirtualRegister curr : operationsBlock.getLiveIn()) { // allocated the already occupied registers
			RegisterBundle registerBundle = curr.getRegisterBundle();
			if (registerBundle != null) {
				freeRegisters.remove(registerBundle);
			}
		}
		return freeRegisters;
	}

	private void assignFreeRegister(RegisterAllocationPolicy policy, Set<RegisterBundle> freeRegisters, VirtualRegister virtualRegister) {
		Set<RegisterBundle> interferringPreallocated = program.getInterferringPreallocatedBundles(virtualRegister);

		HashSet<RegisterBundle> freeNonInterferringRegisters = new HashSet<>(freeRegisters);
		freeNonInterferringRegisters.removeAll(interferringPreallocated);

		RegisterBundle freeBundle = getFreeBundle(policy, freeNonInterferringRegisters, virtualRegister.getPreferedRegisterBundles());
		virtualRegister.setStorage(freeBundle.getRegister(virtualRegister.getMode()));
		freeRegisters.remove(freeBundle);
		program.addUsedRegister(freeBundle);
	}

	private RegisterBundle getFreeBundle(RegisterAllocationPolicy policy, Set<RegisterBundle> freeRegisters, Set<RegisterBundle> preferedRegisters) {
		for (RegisterBundle preferred : preferedRegisters) {
			if (freeRegisters.contains(preferred)) {
				return preferred;
			}
		}

		for (SingleRegister register : policy.getAllowedRegisters(Bit.BIT64)) {
			if (freeRegisters.contains(register.getRegisterBundle())) {
				return register.getRegisterBundle();
			}
		}
		throw new RuntimeException("THIS MAY NEVER HAPPEN!");
	}

	private void checkForOperationAssignments(AssemblerOperation operation, RegisterAllocationPolicy policy, Set<RegisterBundle> freeRegisters) {
		if (operation instanceof CurrentlyAliveRegistersNeeding) {
			Set<RegisterBundle> usedBundles = policy.getAllowedBundles(Bit.BIT64);
			usedBundles.removeAll(freeRegisters);
			((CurrentlyAliveRegistersNeeding) operation).setAliveRegisters(usedBundles);
		}
	}

	public static void debugln(Object o) {
		if (DEBUG)
			System.out.println("DEBUG IG: " + o);
	}

	public static void debug(Object o) {
		if (DEBUG)
			System.out.print(o);
	}

	public static void setDebuggingMode(boolean debug) {
		DEBUG = debug;
	}

}
