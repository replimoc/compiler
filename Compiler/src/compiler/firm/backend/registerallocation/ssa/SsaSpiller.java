package compiler.firm.backend.registerallocation.ssa;

import java.util.Set;

import compiler.firm.FirmUtils;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.nodes.Block;

public class SsaSpiller {
	private static final int STACK_ITEM_SIZE = 8;

	private final AssemblerProgram program;

	private int currentStackOffset = 0;

	public SsaSpiller(AssemblerProgram program) {
		this.program = program;
	}

	public void reduceRegisterPressure(final int availableRegisters) {
		FirmUtils.walkDominanceTree(program.getStartBlock(), new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				reduceRegisterPressure(block, availableRegisters);
			}
		});

	}

	private void reduceRegisterPressure(Block block, int availableRegisters) {
		AssemblerOperationsBlock operationsBlock = program.getOperationsBlock(block);
		Set<VirtualRegister> aliveRegisters = operationsBlock.getLiveIn();

		for (AssemblerOperation operation : operationsBlock.getOperations()) {
			for (RegisterBased readRegisterBased : operation.getReadRegisters()) {
				VirtualRegister readRegister = (VirtualRegister) readRegisterBased;
				if (operationsBlock.isLastUsage(readRegister, operation)) {
					aliveRegisters.remove(readRegister);
				}
			}

			for (RegisterBased writeRegisterBased : operation.getWriteRegisters()) {
				VirtualRegister writeRegister = (VirtualRegister) writeRegisterBased;
				if (writeRegister.getRegister() == null && !writeRegister.isSpilled()) {
					aliveRegisters.add(writeRegister);

					if (aliveRegisters.size() > availableRegisters) {
						spillRegisterOf(aliveRegisters);
					}
				}
			}
		}
	}

	private void spillRegisterOf(Set<VirtualRegister> aliveRegisters) {
		System.err.println("need to spill");
	}

	private void spillRegister(VirtualRegister spilledRegister) {
		spilledRegister.setSpilled(true);
		currentStackOffset += STACK_ITEM_SIZE;
		spilledRegister.setStorage(new MemoryPointer(currentStackOffset, SingleRegister.RSP));
	}
}
