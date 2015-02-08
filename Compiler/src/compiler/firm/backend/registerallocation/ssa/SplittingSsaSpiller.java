package compiler.firm.backend.registerallocation.ssa;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.X8664AssemblerGenerationVisitor;
import compiler.firm.backend.operations.ReloadOperation;
import compiler.firm.backend.operations.dummy.phi.PhiReadOperation;
import compiler.firm.backend.operations.dummy.phi.PhiWriteOperation;
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

	private int currentStackOffset = 0;

	public SplittingSsaSpiller(AssemblerProgram program) {
		this.program = program;
	}

	public void reduceRegisterPressure(final int availableRegisters, final boolean allowSpilling) {
		Map<VirtualRegister, List<ReloadOperation>> insertedReloads = program.executeMinAlgorithm(this, availableRegisters, allowSpilling);
		program.calculateDominanceFrontiers();

		Utils.debugln(true, "inserted reloads: " + insertedReloads);

		program.generatePlainAssemblerFile(".nonSsa");

		reestablishSsaProperty(insertedReloads);
	}

	private void reestablishSsaProperty(Map<VirtualRegister, List<ReloadOperation>> insertedReloads) {
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

			Map<AssemblerOperation, VirtualRegister> definitions = new HashMap<>();
			Set<AssemblerOperationsBlock> definitionsBlocks = new HashSet<>();

			Utils.debugln(DEBUG_VR_REPLACING, "replacing " + spilledRegister + " definined in " + spilledRegister.getDefinition());
			Bit mode = spilledRegister.getMode();
			for (ReloadOperation currReload : reloadEntry.getValue()) {
				Utils.debugln(DEBUG_VR_REPLACING, currReload);
				// replace old register with a new one
				VirtualRegister newRegister = new VirtualRegister(mode);
				newRegister.setDefinition(currReload);
				replaceVirtualRegister(currReload, spilledRegister, newRegister);
				Utils.debugln(DEBUG_VR_REPLACING, currReload);

				// calculate maps
				definitions.put(currReload, newRegister);
				definitionsBlocks.add(currReload.getOperationsBlock());
			}
			definitions.put(spilledRegister.getDefinition(), spilledRegister);
			definitionsBlocks.add(spilledRegister.getDefinition().getOperationsBlock());

			for (AssemblerOperation use : spilledRegister.getUsages()) {
				VirtualRegister newDefinition = findDefinition(use, use.getOperationsBlock(), definitions, definitionsBlocks,
						iteratedDominanceFrontier, mode);
				replaceVirtualRegister(use, spilledRegister, newDefinition);
			}
		}

		program.calculateLiveInAndLiveOut();
	}

	private VirtualRegister findDefinition(AssemblerOperation useInBlock, AssemblerOperationsBlock block,
			Map<AssemblerOperation, VirtualRegister> definitions,
			Set<AssemblerOperationsBlock> definitionsBlocks, Set<AssemblerOperationsBlock> iteratedDominanceFrontier, Bit mode) {

		while (block != null) {
			if (definitionsBlocks.contains(block)) { // the current block contains a definition, use it.
				VirtualRegister foundDefinition = block.findDefinition(useInBlock, definitions);
				if (foundDefinition != null) {
					return foundDefinition;
				}
			}

			if (iteratedDominanceFrontier.contains(block)) { // we need to insert a phi
				VirtualRegister phiResult = new VirtualRegister(mode);

				PhiWriteOperation phiWrite = block.getPhiWrite();
				phiWrite.addWriteRegister(phiResult);
				definitions.put(phiWrite, phiResult); // add the new definition

				for (AssemblerOperationsBlock predecessor : block.getPredecessors()) {
					PhiReadOperation phiRead = predecessor.getPhiRead();
					VirtualRegister source = findDefinition(null, predecessor, definitions, definitionsBlocks, iteratedDominanceFrontier, mode);
					phiRead.addPhiRelation(source, phiResult);
				}
				return phiResult;
			}

			block = program.getIDom(block); // walk dominator tree upwards
		}

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
					} else if (fieldObject instanceof Collection && fieldObject != null) {
						for (Object fieldItem : ((Collection<?>) fieldObject)) {
							replaceVirtualRegister(fieldItem, originalRegister, replacementRegister);
						}
					}
				}
				classType = classType.getSuperclass();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
