package compiler.firm.backend.registerallocation.ssa;

import java.lang.reflect.Field;
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
import compiler.utils.Utils;

public class SplittingSsaSpiller implements StackInfoSupplier {

	private static final boolean DEBUG_I_DOMINANCE_FRONTIER = true;
	private static final boolean DEBUG_VR_REPLACING = false;

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

		Utils.debugln(false, insertedReloads);

		for (Entry<VirtualRegister, List<ReloadOperation>> reloadEntry : insertedReloads.entrySet()) {
			Set<AssemblerOperationsBlock> labelBlocks = new HashSet<>();
			VirtualRegister originalRegister = reloadEntry.getKey();
			labelBlocks.add(originalRegister.getDefinition().getOperationsBlock());
			for (ReloadOperation reloadOperation : reloadEntry.getValue()) {
				labelBlocks.add(reloadOperation.getOperationsBlock());
			}

			Set<AssemblerOperationsBlock> iteratedDominanceFrontier = calculateIteratedDominanceFrontier(labelBlocks);
			Utils.debugln(DEBUG_I_DOMINANCE_FRONTIER, "iterated dominance border for " + originalRegister + " with blocks " + labelBlocks
					+ "    is: " + iteratedDominanceFrontier);

			HashMap<ReloadOperation, VirtualRegister> registerReplacements = new HashMap<>();

			Utils.debugln(DEBUG_VR_REPLACING, "replacing " + originalRegister + " definined in " + originalRegister.getDefinition());
			for (ReloadOperation currReload : reloadEntry.getValue()) {
				VirtualRegister newRegister = new VirtualRegister(originalRegister.getMode());
				registerReplacements.put(currReload, newRegister);
				Utils.debugln(DEBUG_VR_REPLACING, currReload);
				replaceVirtualRegister(currReload, originalRegister, newRegister);
				Utils.debugln(DEBUG_VR_REPLACING, currReload);
			}
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

	private static void replaceVirtualRegister(Object object, VirtualRegister originalRegister, VirtualRegister replacementRegister) {
		try {
			Class<?> classType = object.getClass();
			while (classType != null) {
				for (Field field : classType.getDeclaredFields()) {
					field.setAccessible(true);
					Object fieldObject = field.get(object);
					if (fieldObject == originalRegister) {
						field.set(object, replacementRegister);
					} else if (fieldObject instanceof MemoryPointer) {
						replaceVirtualRegister(fieldObject, originalRegister, replacementRegister);
					}
				}
				classType = classType.getSuperclass();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
