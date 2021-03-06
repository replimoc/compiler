package compiler.firm.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.operations.MovOperation;
import compiler.firm.backend.operations.SwapOperation;
import compiler.firm.backend.storage.Constant;
import compiler.firm.backend.storage.MemoryPointer;
import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;
import compiler.utils.Pair;

public final class TransferGraphSolver {

	private TransferGraphSolver() {
	}

	public static List<String> calculateOperations(List<Pair<Storage, RegisterBased>> transferFunctions) {
		List<String> result = new ArrayList<>();
		List<String> saveMoves = new ArrayList<>();

		HashMap<Key, List<RegisterBased>> phiFromToGraph = new LinkedHashMap<>(); // graph with directed out-edges
		HashMap<Key, RegisterBased> phiToFromGraph = new LinkedHashMap<>(); // graph with directed in-edges

		for (Pair<Storage, RegisterBased> curr : transferFunctions) { // handle constant assignments and calculate graph for register assignments
			Storage source = curr.first;
			RegisterBased destination = curr.second;

			if (source == destination) {
				// do nothing for moves from register into itself

			} else if (source instanceof Constant || source instanceof MemoryPointer) {
				saveMoves.addAll(Arrays.asList(new MovOperation("transfer: " + destination.getComment(),
						source, destination).toStringWithSpillcode()));

			} else { // add this relation to the graph
				List<RegisterBased> destinations = phiFromToGraph.get(new Key((RegisterBased) source));
				if (destinations == null) {
					destinations = new LinkedList<>();
					phiFromToGraph.put(new Key((RegisterBased) source), destinations);
				}
				destinations.add(destination);
				phiToFromGraph.put(new Key(destination), (RegisterBased) source);
			}
		}

		while (true) { // phase 1: remove all non-cyclic phis
			List<Key> withoutOutEdges = new LinkedList<>();
			for (Entry<Key, RegisterBased> curr : phiToFromGraph.entrySet()) {
				if (!phiFromToGraph.containsKey(curr.getKey())) {
					withoutOutEdges.add(curr.getKey());
				}
			}

			if (withoutOutEdges.isEmpty()) // we have no target registers without an out-edge left => continue with next phase
				break;

			for (Key destinationKey : withoutOutEdges) {
				RegisterBased currentSource = phiToFromGraph.get(destinationKey);
				Key sourceKey = new Key(currentSource);

				result.addAll(Arrays.asList(new MovOperation("transfer: " + currentSource.getComment() + " -> "
						+ destinationKey.register.getComment(),
						currentSource, destinationKey.register).toStringWithSpillcode()));
				phiToFromGraph.remove(destinationKey); // remove this edge
				List<RegisterBased> changedTargets = phiFromToGraph.remove(sourceKey);
				changedTargets.remove(destinationKey.register);
				if (!changedTargets.isEmpty())
					phiFromToGraph.put(destinationKey, changedTargets); // put edges of source to the new copy

				for (RegisterBased targetRegister : changedTargets) {
					Key targetKey = new Key(targetRegister);
					phiToFromGraph.remove(targetKey);
					phiToFromGraph.put(targetKey, destinationKey.register);
				}
			}
		}

		while (!phiToFromGraph.isEmpty()) { // phase 2: all other entries are cycles => handle cycles with swaps around the cycle
			Key start = phiToFromGraph.keySet().iterator().next();

			LinkedList<RegisterBased> cycleStack = new LinkedList<>();
			RegisterBased curr = start.register;
			while ((curr = phiToFromGraph.remove(new Key(curr))) != null) {
				cycleStack.add(curr);
			}

			RegisterBased last = cycleStack.removeFirst();
			while (!cycleStack.isEmpty()) {
				curr = cycleStack.removeFirst();
				result.addAll(Arrays.asList(new SwapOperation("phi: " + last.getComment() + " -> " + curr.getComment(), last, curr)
						.toStringWithSpillcode()));
				last = curr;
			}
		}

		result.addAll(saveMoves);

		return result;
	}

	private static class Key {
		final RegisterBased register;

		public Key(RegisterBased register) {
			this.register = register;
		}

		@Override
		public String toString() {
			return register.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (register.isSpilled() ? 1231 : 1237);
			result = prime * result + (!register.isSpilled() ? 0 : register.getMemoryPointer().getOffset());
			result = prime * result + ((register.getRegisterBundle() == null) ? 0 : register.getRegisterBundle().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (register.isSpilled() != other.register.isSpilled())
				return false;
			if (register.isSpilled()) {
				if (register.getMemoryPointer().getOffset() != other.register.getMemoryPointer().getOffset())
					return false;
			} else {
				if (register.getRegisterBundle() == null) {
					if (other.register.getRegisterBundle() != null)
						return false;
				} else if (!register.getRegisterBundle().equals(other.register.getRegisterBundle()))
					return false;
			}
			return true;
		}
	}
}
