package compiler.firm.backend.registerallocation.ssa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.FirmUtils;
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

		for (Entry<VirtualRegister, List<ReloadOperation>> reloadEntry : insertedReloads.entrySet()) {
			Set<AssemblerOperationsBlock> labelBlocks = new HashSet<>();
			labelBlocks.add(reloadEntry.getKey().getDefinition().getOperationsBlock());
			for (ReloadOperation reloadOperation : reloadEntry.getValue()) {
				labelBlocks.add(reloadOperation.getOperationsBlock());
			}

			Set<AssemblerOperationsBlock> iteratedDominanceFrontier = calculateIteratedDominanceFrontier(labelBlocks);
			System.out.println("iterated dominance border for " + reloadEntry.getKey() + " with blocks " + labelBlocks + "    is: "
					+ iteratedDominanceFrontier);
		}
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

	private Set<AssemblerOperationsBlock> calculateIteratedDominanceFrontier(Set<AssemblerOperationsBlock> blocks) {
		Set<AssemblerOperationsBlock> result = new HashSet<>();

		FirmUtils.incrementBlockVisited(program.getGraph());
		LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>(blocks);
		while (!workList.isEmpty()) {
			AssemblerOperationsBlock curr = workList.pop();
			result.addAll(curr.getDominanceFrontier());

			for (AssemblerOperationsBlock frontierElement : curr.getDominanceFrontier()) {
				if (frontierElement.getBlock().blockVisited())
					continue;

				frontierElement.getBlock().markBlockVisited();
				workList.push(frontierElement);
			}
		}

		return result;
	}
}
