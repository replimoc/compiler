package compiler.firm.backend.registerallocation.ssa;

import java.util.Set;

import com.sun.jna.Pointer;
import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.registerallocation.RegisterAllocationPolicy;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

import firm.Graph;
import firm.bindings.binding_irdom;
import firm.nodes.Block;

public class SsaRegisterAllocator {
	public static boolean DEBUG = true;
	private AssemblerProgram program;

	public SsaRegisterAllocator(AssemblerProgram program) {
		this.program = program;
	}

	public void colorGraph(Graph graph, RegisterAllocationPolicy policy) {
		Block startBlock = graph.getStartBlock();
		colorRecursive(startBlock, policy);
	}

	private void colorRecursive(Block block, RegisterAllocationPolicy policy) {
		System.out.println(block);
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
					RegisterBundle freeBundle = getFreeBundle(freeRegisters, writeRegister.getPreferedRegisterBundles());
					writeRegister.setStorage(freeBundle.getRegister(writeRegister.getMode()));
					freeRegisters.remove(freeBundle);
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

	/**
	 * @see Algorithm 4.6 (page 69) of thesis on SSA Register Allocation.
	 * 
	 * @param register1
	 * @param register2
	 * @return
	 */
	private static boolean doVariablesInterfere(VirtualRegister register1, VirtualRegister register2) {
		AssemblerOperation definition1 = register1.getDefinition();
		AssemblerOperation definition2 = register2.getDefinition();

		VirtualRegister dominating;
		VirtualRegister dominated;

		if (dominates(definition1, definition2)) {
			dominating = register1;
			dominated = register2;
		} else if (dominates(definition2, definition1)) {
			dominating = register2;
			dominated = register1;
		} else {
			return false;
		}

		if (dominated.getDefinition().getOperationsBlock().getLiveOut().contains(dominating))
			return true;

		for (AssemblerOperation usage : dominating.getUsages()) {
			if (dominates(dominated.getDefinition(), usage)) {
				return true; // if the definition of the dominated dominates a usage of the dominating, they interfere
			}
		}

		return false;
	}

	private static boolean dominates(AssemblerOperation operation1, AssemblerOperation operation2) {
		AssemblerOperationsBlock operationsBlock1 = operation1.getOperationsBlock();
		AssemblerOperationsBlock operationsBlock2 = operation2.getOperationsBlock();

		if (operationsBlock1 == operationsBlock2) {
			return operationsBlock1.strictlyDominates(operation1, operation2);
		} else {
			return operationsBlock1.dominates(operationsBlock2);
		}
	}

	private RegisterBundle getFreeBundle(Set<RegisterBundle> freeRegisters, Set<RegisterBundle> preferedRegisters) {
		for (RegisterBundle preferred : preferedRegisters) {
			if (freeRegisters.contains(preferred)) {
				return preferred;
			}
		}

		return freeRegisters.iterator().next();
	}

	private void checkForOperationAssignments(AssemblerOperation operation, RegisterAllocationPolicy policy, Set<RegisterBundle> freeRegisters) {
		if (operation instanceof CallOperation) {
			Set<RegisterBundle> usedBundles = policy.getAllowedBundles(Bit.BIT64);
			usedBundles.removeAll(freeRegisters);
			((CallOperation) operation).addAliveRegisters(usedBundles);
		}
	}
}
