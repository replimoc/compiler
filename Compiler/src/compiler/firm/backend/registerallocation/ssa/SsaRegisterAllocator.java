package compiler.firm.backend.registerallocation.ssa;

import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Pointer;
import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

import firm.Graph;
import firm.bindings.binding_irdom;
import firm.nodes.Block;

public class SsaRegisterAllocator {
	public static boolean DEBUG = true;
	private final AssemblerProgram program;

	public SsaRegisterAllocator(AssemblerProgram program) {
		this.program = program;
	}

	public void colorGraph(Graph graph, RegisterAllocationPolicy policy) {
		Block startBlock = graph.getStartBlock();
		colorRecursive(startBlock, policy);

		System.out.println("used registers (" + program.getUsedRegisters().size() + "): " + program.getUsedRegisters());
	}

	private void colorRecursive(Block block, RegisterAllocationPolicy policy) {
		AssemblerOperationsBlock operationsBlock = program.getOperationsBlock(block);

		Set<RegisterBundle> freeRegisters = policy.getAllowedBundles(Bit.BIT64);

		for (VirtualRegister curr : operationsBlock.getLiveIn()) { // allocated the already occupied registers
			RegisterBundle registerBundle = curr.getRegisterBundle();
			if (registerBundle != null) {
				freeRegisters.remove(registerBundle);
			}
		}

		for (AssemblerOperation operation : operationsBlock.getOperations()) {
			for (RegisterBased readRegisterBased : operation.getReadRegisters()) {
				VirtualRegister readRegister = (VirtualRegister) readRegisterBased;
				if (operationsBlock.isLastUsage(readRegister, operation)) {
					freeRegisters.add(readRegister.getRegisterBundle());
				}
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				if (writeRegister.getRegister() == null) {
					assignFreeRegister(policy, freeRegisters, writeRegister);
				}
			}

			checkForOperationAssignments(operation, policy, freeRegisters); // irrelevant for register allocation
		}

		for (Pointer dominatedPtr = binding_irdom.get_Block_dominated_first(block.ptr); dominatedPtr != null; dominatedPtr = binding_irdom
				.get_Block_dominated_next(dominatedPtr)) {
			Block dominatedBlock = new Block(dominatedPtr);
			colorRecursive(dominatedBlock, policy);
		}
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
		if (operation instanceof CallOperation) {
			Set<RegisterBundle> usedBundles = policy.getAllowedBundles(Bit.BIT64);
			usedBundles.removeAll(freeRegisters);
			((CallOperation) operation).addAliveRegisters(usedBundles);
		}
	}

}
