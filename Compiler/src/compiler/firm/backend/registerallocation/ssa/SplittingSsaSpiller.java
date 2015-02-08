package compiler.firm.backend.registerallocation.ssa;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.ReloadOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
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
		program.executeMinAlgorithm(this, availableRegisters, allowSpilling);
		program.calculateDominanceFrontiers();

		Utils.debugln(false, insertedReloads);

		for (Entry<VirtualRegister, List<ReloadOperation>> reloadEntry : insertedReloads.entrySet()) {
			Set<AssemblerOperationsBlock> labelBlocks = new HashSet<>();
			VirtualRegister spilledRegister = reloadEntry.getKey();
			labelBlocks.add(spilledRegister.getDefinition().getOperationsBlock());
			for (ReloadOperation reloadOperation : reloadEntry.getValue()) {
				labelBlocks.add(reloadOperation.getOperationsBlock());
			}

			Set<AssemblerOperationsBlock> iteratedDominanceFrontier = program.calculateIteratedDominanceFrontier(labelBlocks);
			Utils.debugln(DEBUG_I_DOMINANCE_FRONTIER, "iterated dominance border for " + spilledRegister + " with blocks " + labelBlocks
					+ "    is: " + iteratedDominanceFrontier);

			Set<VirtualRegister> definitions = new HashSet<>();

			Utils.debugln(DEBUG_VR_REPLACING, "replacing " + spilledRegister + " definined in " + spilledRegister.getDefinition());
			for (ReloadOperation currReload : reloadEntry.getValue()) {
				VirtualRegister newRegister = new VirtualRegister(spilledRegister.getMode());
				Utils.debugln(DEBUG_VR_REPLACING, currReload);
				replaceVirtualRegister(currReload, spilledRegister, newRegister);
				Utils.debugln(DEBUG_VR_REPLACING, currReload);
				definitions.add(newRegister);
				newRegister.setDefinition(currReload);
			}
			definitions.add(spilledRegister);

			for (AssemblerOperation use : spilledRegister.getUsages()) {
				VirtualRegister newDefinition = findDefinition(use, definitions, iteratedDominanceFrontier);
				replaceVirtualRegister(use, spilledRegister, newDefinition);
			}
		}
	}

	private VirtualRegister findDefinition(AssemblerOperation use, Set<VirtualRegister> definitions,
			Set<AssemblerOperationsBlock> iteratedDominanceFrontier) {
		// TODO Auto-generated method stub
		return null;
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
