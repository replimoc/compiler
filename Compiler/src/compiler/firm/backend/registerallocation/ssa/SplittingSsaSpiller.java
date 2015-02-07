package compiler.firm.backend.registerallocation.ssa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.ReloadOperation;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;

public class SplittingSsaSpiller implements StackInfoSupplier {

	private final AssemblerProgram program;
	private final Map<VirtualRegister, MemoryPointer> stackLocations = new HashMap<>();
	private final Map<VirtualRegister, List<ReloadOperation>> insertedReloads = new HashMap<>();

	private int currentStackOffset = 0;

	public SplittingSsaSpiller(AssemblerProgram program) {
		this.program = program;
	}

	public void reduceRegisterPressure(final int availableRegisters, final boolean allowSpilling) {
		currentStackOffset = 0; // reset state

		program.walkBlocksReversePostorder(new AssemblerOperationsBlockWalker() {
			@Override
			public void visitBlock(AssemblerOperationsBlock operationsBlock) {
				reduceRegisterPressure(operationsBlock, availableRegisters, allowSpilling);
			}
		});

		program.walkBlocksPostorder(new AssemblerOperationsBlockWalker() {
			@Override
			public void visitBlock(AssemblerOperationsBlock block) {
				block.calculateDominanceFrontier();
			}
		});

		System.out.println(insertedReloads);
	}

	private void reduceRegisterPressure(AssemblerOperationsBlock operationsBlock, int availableRegisters, boolean allowSpilling) {
		if (operationsBlock == null) {
			return;
		}

		operationsBlock.executeMinAlgorithm(insertedReloads, availableRegisters, this);
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
