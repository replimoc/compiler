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
import java.util.Set;

import compiler.firm.backend.Bit;
import compiler.firm.backend.storage.RegisterBundle;
import compiler.firm.backend.storage.SingleRegister;
import compiler.firm.backend.storage.VirtualRegister;
import compiler.utils.Pair;

public class InterferenceGraph {
	private static boolean DEBUG = true;

	private final LinkedHashMap<VirtualRegister, LinkedHashSet<VirtualRegister>> graph = new LinkedHashMap<>();

	/**
	 * Copy constructor.
	 * 
	 * @param interferenceGraph
	 *            {@link InterferenceGraph} to be copied.
	 */
	public InterferenceGraph(InterferenceGraph interferenceGraph) {
		for (Entry<VirtualRegister, LinkedHashSet<VirtualRegister>> curr : interferenceGraph.graph.entrySet()) {
			this.graph.put(curr.getKey(), new LinkedHashSet<>(curr.getValue()));
		}
	}

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
		debug("\n");

		// sort the intervals by their start
		Collections.sort(allIntervals, new Comparator<Pair<Interval, VirtualRegister>>() {
			@Override
			public int compare(Pair<Interval, VirtualRegister> i1, Pair<Interval, VirtualRegister> i2) {
				return i1.first.getStart() - i2.first.getStart();
			}
		});

		debugln("allIntervals: " + allIntervals);

		createInterferenceGraphFromIntervals(allIntervals);

		debugln("interferences graph: " + graph);
	}

	private void createInterferenceGraphFromIntervals(List<Pair<Interval, VirtualRegister>> allIntervals) {
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

	public static LinkedHashSet<RegisterBundle> allocateRegisters(InterferenceGraph inputGraph, RegisterAllocationPolicy allocationPolicy,
			List<VirtualRegister> removedRegisters) {

		// color graph
		LinkedHashSet<RegisterBundle> usedRegisters = new LinkedHashSet<RegisterBundle>();
		for (VirtualRegister curr : removedRegisters) {
			if (curr.getRegister() != null) {
				continue;
			}

			RegisterBundle freeBundle = inputGraph.getFreeRegisterBundle(curr, allocationPolicy);
			curr.setStorage(freeBundle.getRegister(curr.getMode()));
			usedRegisters.add(freeBundle);
		}

		debugln("colored registers: " + removedRegisters);

		return usedRegisters;
	}

	public static RemoveResult calculateRemoveListAndSpills(InterferenceGraph inputGraph, int availableRegisters) {
		InterferenceGraph graph = new InterferenceGraph(inputGraph); // create a copy for the first steps

		LinkedList<VirtualRegister> spilledRegisters = new LinkedList<>();
		LinkedList<VirtualRegister> removedRegisters = new LinkedList<>();

		// remove nodes according to heuristic
		while (!graph.isEmpty()) {
			VirtualRegister nextRegister = graph.selectNodeWithLessInterferences(availableRegisters);
			if (nextRegister == null) {
				VirtualRegister registerToSpill = graph.getNodeWithMaxInterferences();
				if (registerToSpill != null) { // spill register
					graph.remove(registerToSpill);
					spilledRegisters.add(registerToSpill);
					continue;
				} else {
					removedRegisters.addAll(graph.getNodes());
					break; // all spillable registers are spilled, but graph contains nodes => these are fixed registers
				}
			}

			removedRegisters.push(nextRegister);
			graph.remove(nextRegister);
		}
		debugln("spilled: " + spilledRegisters);
		debugln("colorable; removed: " + removedRegisters);

		return new RemoveResult(spilledRegisters, removedRegisters);
	}

	public Set<VirtualRegister> getNodes() {
		return graph.keySet();
	}

	public boolean isEmpty() {
		return graph.isEmpty();
	}

	public void remove(VirtualRegister register) {
		LinkedHashSet<VirtualRegister> edges = graph.remove(register); // remove node

		if (edges != null) { // remove edges leading back
			for (VirtualRegister edgeNode : edges) {
				graph.get(edgeNode).remove(register);
			}
		}
	}

	private VirtualRegister getNodeWithMaxInterferences() {
		VirtualRegister maxRegister = null;
		int maxInterferences = -1;
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

	private RegisterBundle getFreeRegisterBundle(VirtualRegister register, RegisterAllocationPolicy allocationPolicy) {
		LinkedHashSet<VirtualRegister> interferringNodes = graph.get(register);
		SingleRegister[] availableRegisters = allocationPolicy.getAllowedRegisters(Bit.BIT64);

		OUTER: for (SingleRegister currRegister : availableRegisters) {
			RegisterBundle currBundle = currRegister.getRegisterBundle();

			for (VirtualRegister currEdgeNode : interferringNodes) {// check if interfering registers use this one
				if (currEdgeNode.getRegister() != null && currEdgeNode.getRegisterBundle() == currBundle) {
					continue OUTER;
				}
			}
			return currBundle;
		}

		throw new RuntimeException("THIS MAY NEVER HAPPEN!");
	}

	private VirtualRegister selectNodeWithLessInterferences(int availableRegisters) {
		for (Entry<VirtualRegister, LinkedHashSet<VirtualRegister>> currEntry : graph.entrySet()) {
			if (currEntry.getKey().getRegister() == null && currEntry.getValue().size() < availableRegisters) {
				return currEntry.getKey();
			}
		}
		return null;
	}

	private static void debugln(Object o) {
		if (DEBUG)
			System.out.println("DEBUG IG: " + o);
	}

	private static void debug(Object o) {
		if (DEBUG)
			System.out.print(o);
	}

	public static void setDebuggingMode(boolean debug) {
		DEBUG = debug;
	}
}
