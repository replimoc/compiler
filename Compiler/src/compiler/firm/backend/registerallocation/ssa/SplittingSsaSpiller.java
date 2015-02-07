package compiler.firm.backend.registerallocation.ssa;

import java.util.HashMap;
import java.util.Map;

import compiler.firm.FirmUtils;
import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.nodes.Block;

public class SplittingSsaSpiller implements StackInfoSupplier {

	private final AssemblerProgram program;
	private final Map<VirtualRegister, MemoryPointer> stackLocations = new HashMap<>();

	private int currentStackOffset = 0;

	public SplittingSsaSpiller(AssemblerProgram program) {
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

		operationsBlock.executeMinAlgorithm(availableRegisters, this);
	}

	public int getCurrentStackOffset() {
		return currentStackOffset;
	}

	@Override
	public MemoryPointer getStackLocation(VirtualRegister register) {
		return stackLocations.get(register);
	}

	@Override
	public MemoryPointer allocateStackLocation(VirtualRegister register) {
		MemoryPointer newStackLocation = new MemoryPointer(currentStackOffset, SingleRegister.RSP);
		stackLocations.put(register, newStackLocation);
		currentStackOffset += X8664AssemblerGenerationVisitor.STACK_ITEM_SIZE;
		return newStackLocation;
	}
}
