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

public class PhiGraph {

	private List<Pair<Storage, RegisterBased>> phiRelations;

	public PhiGraph(List<Pair<Storage, RegisterBased>> phiRelations) {
		this.phiRelations = phiRelations;
	}

	public List<String> calculateOperations() {
		List<String> result = new ArrayList<>();

		HashMap<Key, List<RegisterBased>> phiFromToGraph = new HashMap<>(); // graph with directed out-edges
		HashMap<Key, RegisterBased> phiToFromGraph = new HashMap<>(); // graph with directed in-edges

		for (Pair<Storage, RegisterBased> curr : phiRelations) {
			Storage source = curr.first;
			RegisterBased destination = curr.second;

			if (source == destination) {
				// do nothing for moves from register into itself

			} else if (source instanceof Constant) {
				result.addAll(Arrays.asList(new MovOperation("phi", source, destination).toStringWithSpillcode()));

			} else if (source instanceof MemoryPointer) {
				System.err.println("SHIT");

			} else {

				List<RegisterBased> destinations = phiFromToGraph.get(new Key((RegisterBased) source));
				if (destinations == null) {
					destinations = new LinkedList<>();
					phiFromToGraph.put(new Key((RegisterBased) source), destinations);
				}
				destinations.add(destination);
				phiToFromGraph.put(new Key(destination), (RegisterBased) source);
			}
		}

		while (true) { // remove all non-cyclic phis
			List<RegisterBased> withoutOutEdges = new LinkedList<>();
			for (Entry<Key, RegisterBased> curr : phiToFromGraph.entrySet()) {
				if (!phiFromToGraph.containsKey(curr.getKey())) {
					withoutOutEdges.add(curr.getKey().register);
				}
			}

			if (withoutOutEdges.isEmpty())
				break;

			for (RegisterBased currentDestination : withoutOutEdges) {
				Key destinationKey = new Key(currentDestination);
				RegisterBased currentSource = phiToFromGraph.get(destinationKey);
				Key sourceKey = new Key(currentSource);

				result.addAll(Arrays.asList(new MovOperation("phi", currentSource, currentDestination).toStringWithSpillcode()));
				phiToFromGraph.remove(destinationKey); // remove this edge
				List<RegisterBased> changedTargets = phiFromToGraph.remove(sourceKey);
				changedTargets.remove(currentDestination);
				if (!changedTargets.isEmpty())
					phiFromToGraph.put(sourceKey, changedTargets); // put edges of source to the new copy

				for (RegisterBased targetRegister : changedTargets) {
					phiToFromGraph.remove(new Key(targetRegister));
					phiToFromGraph.put(new Key(targetRegister), currentSource);
				}
			}
		}

		while (!phiToFromGraph.isEmpty()) { // all other entries are cycles
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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			if (register.getRegisterBundle() == null) {
				if (other.register.getRegisterBundle() != null)
					return false;
			} else if (!register.getRegisterBundle().equals(other.register.getRegisterBundle()))
				return false;
			return true;
		}

	}
}
