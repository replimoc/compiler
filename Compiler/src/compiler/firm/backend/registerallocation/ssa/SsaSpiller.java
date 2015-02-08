package compiler.firm.backend.registerallocation.ssa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.nodes.Block;

public class SsaSpiller {

	private final AssemblerProgram program;

	private int currentStackOffset = 0;

	public SsaSpiller(AssemblerProgram program) {
		this.program = program;
	}

	public void reduceRegisterPressure(final int availableRegisters, final boolean allowSpilling) {
		currentStackOffset = 0; // reset state

		FirmUtils.walkDominanceTree(program.getStartBlock(), new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				reduceRegisterPressure(block, availableRegisters, allowSpilling);
			}
		});
	}

	private void reduceRegisterPressure(Block block, int availableRegisters, boolean allowSpilling) {
		AssemblerOperationsBlock operationsBlock = program.getOperationsBlock(block);
		if (operationsBlock == null) {
			return;
		}

		Set<VirtualRegister> aliveRegisters = new HashSet<>(operationsBlock.getLiveIn());
		for (Iterator<VirtualRegister> iterator = aliveRegisters.iterator(); iterator.hasNext();) {
			if (iterator.next().isSpilled()) {
				iterator.remove();
			}
		}

		for (AssemblerOperation operation : operationsBlock.getOperations()) {
			for (VirtualRegister readRegister : operation.getVirtualReadRegisters()) {
				if (operationsBlock.isLastUsage(readRegister, operation)) {
					aliveRegisters.remove(readRegister);
				}
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				if (writeRegister.getRegister() == null && !writeRegister.isSpilled()) {
					aliveRegisters.add(writeRegister);

					if (aliveRegisters.size() > availableRegisters) {
						if (!allowSpilling) {
							throw new MustSpillException();
						}
						spillRegisterOf(operation, aliveRegisters);
					}
				}
			}
		}
	}

	private void spillRegisterOf(AssemblerOperation operation, Set<VirtualRegister> aliveRegisters) {
		VirtualRegister toBeSpilled = aliveRegisters.iterator().next();
		int spilledUseDistance = operation.getOperationsBlock().getNextUseDistance(operation, toBeSpilled, true);

		for (VirtualRegister curr : aliveRegisters) {
			int currUseDistance = operation.getOperationsBlock().getNextUseDistance(operation, curr, true);
			if (spilledUseDistance < currUseDistance) {
				toBeSpilled = curr;
				spilledUseDistance = currUseDistance;
			}
		}

		SsaRegisterAllocator.debugln("spilling register: VR_" + toBeSpilled.getNum());
		aliveRegisters.remove(toBeSpilled);
		spillRegister(toBeSpilled);
	}

	private void spillRegister(VirtualRegister spilledRegister) {
		spilledRegister.setSpilled(true);
		currentStackOffset += X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE;
		spilledRegister.setStorage(new MemoryPointer(currentStackOffset, SingleRegister.RSP));
	}

	public int getCurrentStackOffset() {
		return currentStackOffset;
	}
}
