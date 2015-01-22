package compiler.firm.backend.registerallocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;

public class InterferenceGraph {
	private static final boolean DEBUG = true;

	private final LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph = new LinkedHashMap<>();

	public InterferenceGraph(List<VirtualRegister> registers) {
		debug("DEBUG IG: registers:");

		// create a list of all intervals with their according register
		List<Pair<Interval, VirtualRegister>> allIntervals = new ArrayList<>();
		for (VirtualRegister curr : registers) {
			debug(" VR_" + curr.getNum() + ": ");
			for (Interval interval : curr.getLiftimeIntervals()) {
				allIntervals.add(new Pair<>(interval, curr));
				debug(interval);
			}
		}

		// sort the intervals by their start
		Collections.sort(allIntervals, new Comparator<Pair<Interval, VirtualRegister>>() {
			@Override
			public int compare(Pair<Interval, VirtualRegister> i1, Pair<Interval, VirtualRegister> i2) {
				return i1.first.getStart() - i2.first.getStart();
			}
		});

		debugln("allIntervals: " + allIntervals);

		// create interference graph from intervals
		List<Pair<Interval, VirtualRegister>> activeIntervals = new LinkedList<>();
		for (Pair<Interval, VirtualRegister> currInterval : allIntervals) {
			int currStart = currInterval.first.getStart();
			// remove outdated intervals
			for (Iterator<Pair<Interval, VirtualRegister>> iterator = activeIntervals.iterator(); iterator.hasNext();) {
				Interval currActive = iterator.next().first;
				if (currActive.getEnd() <= currStart) {
					iterator.remove();
				}
			}

			// save register with interfering ones

			addInterferences(currInterval.second, activeIntervals);
			activeIntervals.add(currInterval);
		}

		debugln("interferences graph: " + graph);
	}

	private void addInterferences(VirtualRegister currRegister, List<Pair<Interval, VirtualRegister>> activeIntervals) {
		LinkedHashSet<VirtualRegister> interfering = graph.get(currRegister);
		if (interfering == null) {
			interfering = new LinkedHashSet<>();
			graph.put(currRegister, interfering);
		}

		for (Pair<Interval, VirtualRegister> curr : activeIntervals) {
			graph.get(curr.second).add(currRegister);
			interfering.add(curr.second);
		}
	}

	public AllocationResult allocateRegisters(RegisterAllocationPolicy allocationPolicy) { // color graph
		LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph = copyGraph(this.graph);
		int availableRegisters = allocationPolicy.getNumberOfRegisters(Bit.BIT64);

		LinkedList<VirtualRegister> spilledRegisters = new LinkedList<>();
		LinkedList<VirtualRegister> removedRegisters = new LinkedList<>();

		// remove nodes according to heuristic
		while (!graph.isEmpty()) {
			VirtualRegister nextRegister = selectNode(graph, availableRegisters);
			if (nextRegister == null) {
				VirtualRegister spillRegister = getNodeWithMaxInterferences(graph);
				remove(graph, spillRegister);
				remove(this.graph, spillRegister);
				spilledRegisters.add(spillRegister);
				continue;
			}

			removedRegisters.push(nextRegister);
			remove(graph, nextRegister);
		}
		debugln("spilled: " + spilledRegisters);
		debugln("colorable; removed: " + removedRegisters);

		LinkedHashSet<RegisterBundle> usedRegisters = new LinkedHashSet<RegisterBundle>();
		graph = this.graph;
		for (VirtualRegister curr : removedRegisters) {
			if (curr.getRegister() != null) {
				continue;
			}

			RegisterBundle freeBundle = getFreeRegisterBundle(allocationPolicy, graph.get(curr));
			curr.setStorage(freeBundle.getRegister(curr.getMode()));
			usedRegisters.add(freeBundle);
		}

		debugln("colored registers: " + removedRegisters);

		return new AllocationResult(spilledRegisters, usedRegisters);
	}

	private LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> copyGraph(
			LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph) {
		LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> result = new LinkedHashMap<>();

		for (Entry<VirtualRegister, LinkedHashSet<VirtualRegister>> curr : graph.entrySet()) {
			result.put(curr.getKey(), new LinkedHashSet<>(curr.getValue()));
		}

		return result;
	}

	private VirtualRegister getNodeWithMaxInterferences(LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph) {
		VirtualRegister maxRegister = null;
		int maxInterferences = 0;
		for (Entry<VirtualRegister, LinkedHashSet<VirtualRegister>> curr : graph.entrySet()) {
			VirtualRegister register = curr.getKey();
			int numberOfEdges = curr.getValue().size();
			if (register.getRegister() == null && numberOfEdges > maxInterferences) {
				maxInterferences = numberOfEdges;
				maxRegister = register;
			}
		}
		return maxRegister;
	}

	private RegisterBundle getFreeRegisterBundle(RegisterAllocationPolicy allocationPolicy, LinkedHashSet<VirtualRegister> edges) {
		SingleRegister[] availableRegisters = allocationPolicy.getAllowedRegisters(Bit.BIT64);
		OUTER: for (SingleRegister currRegister : availableRegisters) {
			RegisterBundle currBundle = currRegister.getRegisterBundle();

			for (VirtualRegister currEdgeNode : edges) {// check if interfering registers use this one
				if (currEdgeNode.getRegister() != null && currEdgeNode.getRegisterBundle() == currBundle) {
					continue OUTER;
				}
			}
			return currBundle;
		}

		throw new RuntimeException("THIS MAY NEVER HAPPEN!");
	}

	private void remove(LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph, VirtualRegister register) {
		// remove node
		LinkedHashSet<VirtualRegister> edges = graph.remove(register);
		// remove edges leading back
		for (VirtualRegister edgeNode : edges) {
			graph.get(edgeNode).remove(register);
		}
	}

	private VirtualRegister selectNode(LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph, int availableRegisters) {
		for (Entry<VirtualRegister, LinkedHashSet<VirtualRegister>> currEntry : graph.entrySet()) {
			if (currEntry.getValue().size() < availableRegisters) {
				return currEntry.getKey();
			}
		}
		return null;
	}

	private void debugln(Object o) {
		if (DEBUG)
			System.out.println("DEBUG IG: " + o);
	}

	private void debug(Object o) {
		if (DEBUG)
			System.out.print(o);
	}

	public static class AllocationResult {

		public final LinkedList<VirtualRegister> spilledRegisters;
		public final LinkedHashSet<RegisterBundle> usedRegisters;

		public AllocationResult(LinkedList<VirtualRegister> spilledRegisters, LinkedHashSet<RegisterBundle> usedRegisters) {
			this.spilledRegisters = spilledRegisters;
			this.usedRegisters = usedRegisters;
		}
	}
}
