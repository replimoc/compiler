package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.jna.Pointer;
import compiler.firm.backend.Bit;
import compiler.firm.backend.operations.CallOperation;
import compiler.firm.backend.operations.dummy.MethodStartEndOperation;
import compiler.firm.backend.operations.templates.AssemblerOperation;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.VirtualRegister;

import firm.BlockWalker;
import firm.Graph;
import firm.bindings.binding_irdom;
import firm.nodes.Block;

public class SsaRegisterAllocator {
	private static boolean DEBUG = true;

	private final HashMap<Block, AssemblerOperationsBlock> operationsBlocks;

	public SsaRegisterAllocator(Graph graph, HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		this.operationsBlocks = createOperationsBlocks(operationsOfBlocks);
		calculateLiveInAndLiveOut(graph);
	}

	private void calculateLiveInAndLiveOut(Graph graph) {
		final LinkedList<AssemblerOperationsBlock> workList = new LinkedList<>();

		graph.walkBlocks(new BlockWalker() {
			@Override
			public void visitBlock(Block block) {
				AssemblerOperationsBlock operationsBlock = operationsBlocks.get(block);
				if (operationsBlock != null) {
					operationsBlock.calculateTree(operationsBlocks);
					operationsBlock.calculateUsesAndKills();
					workList.add(operationsBlock);
				}
			}
		});

		while (!workList.isEmpty()) {
			AssemblerOperationsBlock operationsBlock = workList.removeLast();
			if (operationsBlock.calculateLiveInAndOut()) {
				workList.addAll(operationsBlock.getPredecessors());
			}
		}

		if (DEBUG) {
			for (Entry<Block, AssemblerOperationsBlock> entry : operationsBlocks.entrySet()) {
				System.out.println(entry.getValue());
			}
		}
	}

	private static HashMap<Block, AssemblerOperationsBlock> createOperationsBlocks(HashMap<Block, ArrayList<AssemblerOperation>> operationsOfBlocks) {
		HashMap<Block, AssemblerOperationsBlock> operationsBlocks = new HashMap<>();

		for (Entry<Block, ArrayList<AssemblerOperation>> entry : operationsOfBlocks.entrySet()) {
			operationsBlocks.put(entry.getKey(), new AssemblerOperationsBlock(entry.getKey(), entry.getValue()));
		}

		return operationsBlocks;
	}

	public void colorGraph(Graph graph, RegisterAllocationPolicy policy) {
		Block startBlock = graph.getStartBlock();
		colorRecursive(startBlock, policy);
	}

	private void colorRecursive(Block block, RegisterAllocationPolicy policy) {
		System.out.println(block);
		AssemblerOperationsBlock operationsBlock = operationsBlocks.get(block);

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
		}

		for (Pointer dominatedPtr = binding_irdom.get_Block_dominated_first(block.ptr); dominatedPtr != null; dominatedPtr = binding_irdom
				.get_Block_dominated_next(dominatedPtr)) {
			Block dominatedBlock = new Block(dominatedPtr);
			colorRecursive(dominatedBlock, policy);
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

	public void setDummyOperationsInformation(Set<RegisterBundle> usedRegisters, int stackSize, boolean isMainMethod,
			RegisterAllocationPolicy policy) {
		if (stackSize > 0) {
			stackSize += 0x10;
			stackSize &= -0x10; // Align to 8-byte.
		}

		for (Entry<Block, AssemblerOperationsBlock> curr : operationsBlocks.entrySet()) {
			ArrayList<AssemblerOperation> operations = curr.getValue().getOperations();
			for (AssemblerOperation operation : operations) {
				if (operation instanceof CallOperation) {
					((CallOperation) operation).addAliveRegisters(policy.getAllowedRegisters(Bit.BIT64));
				}

				if (operation instanceof MethodStartEndOperation) {
					MethodStartEndOperation methodStartEndOperation = (MethodStartEndOperation) operation;
					methodStartEndOperation.setStackOperationSize(stackSize);

					if (isMainMethod) { // if it is the main, no registers need to be saved
						methodStartEndOperation.setUsedRegisters(new HashSet<RegisterBundle>());
					} else {
						methodStartEndOperation.setUsedRegisters(usedRegisters);
					}
				}
			}
		}
	}
}
