package compiler.firm.backend.operations.dummy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public final class PhiGraphSolver {

	private PhiGraphSolver() {
	}

	public static List<String> calculateOperations(List<Pair<Storage, RegisterBased>> phiRelations) {
		List<String> result = new ArrayList<>();

		HashMap<Key, List<RegisterBased>> phiFromToGraph = new HashMap<>(); // graph with directed out-edges
		HashMap<Key, RegisterBased> phiToFromGraph = new HashMap<>(); // graph with directed in-edges

		for (Pair<Storage, RegisterBased> curr : phiRelations) { // handle constant assignments and calculate graph for register assignments
			Storage source = curr.first;
			RegisterBased destination = curr.second;

			if (source == destination) {
				// do nothing for moves from register into itself

			} else if (source instanceof Constant) {
				result.addAll(Arrays.asList(new MovOperation("phi", source, destination).toStringWithSpillcode()));

			} else if (source instanceof MemoryPointer) {
				throw new RuntimeException("No memory pointer expected here!");

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

				result.addAll(Arrays.asList(new MovOperation("phi", currentSource, destinationKey.register).toStringWithSpillcode()));
				phiToFromGraph.remove(destinationKey); // remove this edge
				List<RegisterBased> changedTargets = phiFromToGraph.remove(sourceKey);
				changedTargets.remove(destinationKey.register);
				if (!changedTargets.isEmpty())
					phiFromToGraph.put(sourceKey, changedTargets); // put edges of source to the new copy

				for (RegisterBased targetRegister : changedTargets) {
					Key targetKey = new Key(targetRegister);
					phiToFromGraph.remove(targetKey);
					phiToFromGraph.put(targetKey, currentSource);
				}
			}
		}

		while (!phiToFromGraph.isEmpty()) { // phase 2: all other entries are cycles => handle cycles with swaps around the cycle
			Entry<Key, RegisterBased> start = phiToFromGraph.entrySet().iterator().next();
			RegisterBased currSource = start.getValue();
			RegisterBased currDestination = start.getKey().register;

			do {
				result.addAll(Arrays.asList(new SwapOperation("phi", currSource, currDestination).toStringWithSpillcode()));
				currDestination = currSource;
				currSource = phiToFromGraph.remove(new Key(currDestination));
			} while (currSource != null);
		}

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
