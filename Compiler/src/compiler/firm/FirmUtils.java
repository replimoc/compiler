package compiler.firm;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.jna.Pointer;
import compiler.utils.ExecutionFailedException;
import compiler.utils.Pair;
import compiler.utils.Utils;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Backend;
import firm.BlockWalker;
import firm.ClassType;
import firm.Dump;
import firm.Entity;
import firm.Firm;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Program;
import firm.Relation;
import firm.Type;
import firm.Util;
import firm.bindings.binding_irdom;
import firm.bindings.binding_irgopt;
import firm.bindings.binding_irnode;
import firm.nodes.Add;
import firm.nodes.Address;
import firm.nodes.Block;
import firm.nodes.Call;
import firm.nodes.Cmp;
import firm.nodes.Cond;
import firm.nodes.Const;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;

public final class FirmUtils {

	private static final String JNA_LIBRARY_PATH = "jna.library.path";
	private static final String LIB_FIRM_FOLDER = "lib/firm/";
	private static final String ISA_AMD64 = "isa=amd64";
	private static final String GCC = "gcc";
	private static final String GCC_DEBUG = "-g";

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	public static final String CALLOC_PROXY = "calloc_proxy";

	private FirmUtils() { // no objects of this class shall be created
	}

	public static void initFirm() {
		if (System.getProperty(JNA_LIBRARY_PATH) == null) {
			System.setProperty(JNA_LIBRARY_PATH, Utils.getJarLocation() + File.separator + LIB_FIRM_FOLDER);
		}
		Firm.init();

		// System.out.printf("Initialized libFirm Version: %1s.%s\n", Firm.getMajorVersion(), Firm.getMinorVersion());
	}

	public static void highToLowLevel() {
		for (Type type : Program.getTypes()) {
			if (type instanceof ClassType) {
				layoutClass((ClassType) type);
			}
		}

		for (Entity entity : Program.getGlobalType().getMembers()) {
			entity.setLdIdent(entity.getLdName().replaceAll("[()\\[\\];]", "_"));
		}

		for (Graph graph : Program.getGraphs()) {
			Util.lowerSels(graph);
		}
	}

	private static void layoutClass(ClassType cls) {
		if (cls.equals(Program.getGlobalType()))
			return;

		for (int m = 0; m < cls.getNMembers(); /* nothing */) {
			Entity member = cls.getMember(m);
			Type type = member.getType();
			if (!(type instanceof MethodType)) {
				++m;
				continue;
			}

			/* methods get implemented outside the class, move the entity */
			member.setOwner(Program.getGlobalType());
		}

		cls.layoutFields();
	}

	public static void createAssembler(String outputFileName) throws IOException {
		Backend.option(ISA_AMD64);
		Backend.createAssembler(outputFileName, "<builtin>");
	}

	public interface AssemblerCreator {
		public void create(String file) throws IOException;
	}

	/**
	 * Expect escaped outputFileName.
	 * 
	 * @param outputFileName
	 *            File for the binary executable.
	 * @param debuggingLevel
	 * @throws IOException
	 * @throws ExecutionFailedException
	 */
	public static void createBinary(String outputFileName, String assemblerFile, AssemblerCreator assemblerCreator, String cInclude, String cLibrary,
			String debuggingLevel)
			throws IOException,
			ExecutionFailedException {
		String base = Utils.getJarLocation() + File.separator;
		if (assemblerFile == null) {
			assemblerFile = Utils.createAutoDeleteTempFile("assembler", ".s");
		}

		assemblerCreator.create(assemblerFile);

		List<String> execOptions = new LinkedList<String>();
		execOptions.addAll(Arrays.asList(GCC, "-o", outputFileName));
		if (debuggingLevel != null) {
			execOptions.add(GCC_DEBUG + debuggingLevel);
		}
		execOptions.add(assemblerFile);
		execOptions.add(base + "resources/standardlib.c");

		if (cInclude != null)
			execOptions.add(cInclude);

		if (cLibrary != null)
			execOptions.add("-l" + cLibrary);

		printOutput(Utils.systemExec(execOptions));
	}

	private static void printOutput(Pair<Integer, List<String>> executionState) throws ExecutionFailedException {
		for (String line : executionState.getSecond()) {
			System.out.println(line);
		}

		int exitCode = executionState.getFirst();
		if (exitCode != 0) {
			throw new ExecutionFailedException(exitCode);
		}
	}

	public static void createFirmGraph(String suffix) {
		for (Graph graph : Program.getGraphs()) {
			graph.check();
			Dump.dumpGraph(graph, suffix.isEmpty() ? "generated" : suffix);
		}
	}

	/**
	 * Returns mode of 32-bit integer signed
	 * 
	 * @return
	 */
	public static Mode getModeInteger() {
		return Mode.getIs();
	}

	/**
	 * Returns mode for 8-bit boolean.
	 * 
	 * @return
	 */
	public static Mode getModeBoolean() {
		return Mode.getBu();
	}

	/**
	 * Returns reference mode for 64-bit
	 * 
	 * @return
	 */
	public static Mode getModeReference() {
		return Mode.createReferenceMode("P64", Mode.Arithmetic.TwosComplement, 64, 64);
	}

	public static void finishFirm() {
		Firm.finish();
	}

	public static Node getFirstSuccessor(Node node) {
		return BackEdges.getOuts(node).iterator().next().node;
	}

	public static void removeBadsAndUnreachable(Graph graph) {
		binding_irgopt.remove_unreachable_code(graph.ptr);
		binding_irgopt.remove_bads(graph.ptr);
	}

	public static void replaceNodes(HashMap<Node, Node> replacements) {
		for (Entry<Node, Node> curr : replacements.entrySet()) {
			Graph.exchange(curr.getKey(), curr.getValue());
		}
	}

	public static Node newBad(Node node) {
		return node.getGraph().newBad(node.getMode());
	}

	public static boolean blockPostdominates(Node block, Node block2) {
		return binding_irdom.block_postdominates(block.ptr, block2.ptr) == 1;
	}

	public static boolean blockDominates(Node block, Node block2) {
		return binding_irdom.block_dominates(block.ptr, block2.ptr) == 1;
	}

	public static void walkDominanceTree(Block block, BlockWalker walker) {
		walker.visitBlock(block);

		for (Pointer dominatedPtr = binding_irdom.get_Block_dominated_first(block.ptr); dominatedPtr != null; dominatedPtr = binding_irdom
				.get_Block_dominated_next(dominatedPtr)) {
			Block dominatedBlock = new Block(dominatedPtr);
			walkDominanceTree(dominatedBlock, walker);
		}
	}

	public static boolean isConstant(Node node) {
		return node instanceof Const &&
				(node.getMode().equals(Mode.getIs()) || node.getMode().equals(Mode.getBu()) || node.getMode().equals(Mode.getLu()));
	}

	public static Entity getCalledEntity(Call call) {
		final Address address = (Address) call.getPred(1);
		return address.getEntity();
	}

	public static Block getLoopTailIfHeader(Block block) {
		for (Node pred : block.getPreds()) {
			if (blockDominates(block, pred.getBlock())) {
				return (Block) pred.getBlock();
			}
		}
		return null;
	}

	public static Block getFirstLoopBlock(Cond condition) {
		Block loopContentBlock = null;

		for (Edge projEdge : BackEdges.getOuts(condition)) {
			Proj proj = (Proj) projEdge.node;
			if (BackEdges.getNOuts(proj) == 0)
				return null;

			Node successorBlock = FirmUtils.getFirstSuccessor(proj);

			if (FirmUtils.blockPostdominates(condition.getBlock(), successorBlock)) {
				loopContentBlock = (Block) successorBlock;
			}
		}
		return loopContentBlock;
	}

	private static Long getCycleCount(Cmp cmp, Const constCmp, Const startingValue, Const incr) {
		long start = startingValue.getTarval().asLong();
		long border = constCmp.getTarval().asLong();
		long change = incr.getTarval().asLong();

		Relation relation = cmp.getRelation();
		int minIntOffset = change < 0 ? 1 : 0;
		if (change == 0) {
			return null; // Endless loop
		} else if (change < 0) {
			relation = relation.inversed();
			change = -change;
			start = -start;
			border = -border;
		}
		System.out.println(relation);
		System.out.println("change: " + change);
		System.out.println("start: " + start);
		System.out.println("border: " + border);

		switch (relation) {
		case Greater:
		case UnorderedGreater:
			if (start < border) {
				return (long) Math.ceil((double) Math.abs(border - start) / change);
			} else {
				return (long) Math.ceil((Integer.MAX_VALUE - start + minIntOffset) / change) + 1;
			}
		case GreaterEqual:
		case UnorderedGreaterEqual:
			if (start < border) {
				return (long) Math.ceil((double) Math.abs(border - start + 1) / change);
			} else {
				return (long) Math.ceil((Integer.MAX_VALUE - start + minIntOffset) / change) + 1;
			}
		case Less:
		case UnorderedLess:
			return (long) Math.ceil((double) Math.abs(border - start) / change);
		case LessEqual:
		case UnorderedLessEqual:
			return (long) Math.ceil((double) Math.abs(border - start + 1) / change);
		default:
			return null;
		}

	}

	public static LoopInfo getLoopInfos(Cmp cmp) {
		Block loopHeader = (Block) cmp.getBlock();
		Block loopTail = getLoopTailIfHeader(loopHeader);

		if (loopTail == null)
			return null;

		Const constant = null;
		Node conditionalPhi = null;
		for (Node predecessor : cmp.getPreds()) {
			if (predecessor instanceof Const) {
				constant = (Const) predecessor;
			} else {
				conditionalPhi = predecessor;
			}
		}

		if (constant == null || conditionalPhi == null)
			return null; // Nothing found

		int blockPredecessorLoop = -1;
		for (int i = 0; i < loopHeader.getPredCount(); i++) {
			if (loopHeader.getPred(i).getBlock().equals(loopTail)) {
				blockPredecessorLoop = i;
			}
		}

		if (conditionalPhi instanceof Phi && blockPredecessorLoop >= 0) {
			Block firstLoopBlock = getFirstLoopBlock((Cond) getFirstSuccessor(cmp));

			Node arithmeticNode = conditionalPhi.getPred(blockPredecessorLoop);

			boolean onlyOneNodeBetweenPhi = false;
			if (!(arithmeticNode instanceof Phi)) {
				for (Node arithmeticNodePredecessor : arithmeticNode.getPreds()) {
					if (arithmeticNodePredecessor.equals(conditionalPhi)) {
						onlyOneNodeBetweenPhi = true;
					}
				}
			}

			if (arithmeticNode.getBlock() != null && firstLoopBlock != null && onlyOneNodeBetweenPhi &&
					blockPostdominates(arithmeticNode.getBlock(), firstLoopBlock)) { // Add is always executed
				Const incr = null;
				if (arithmeticNode.getPredCount() > 1 && isConstant(arithmeticNode.getPred(1)) && arithmeticNode instanceof Add) {
					incr = (Const) arithmeticNode.getPred(1);
				} else if (arithmeticNode.getPredCount() > 1 && isConstant(arithmeticNode.getPred(0)) && arithmeticNode instanceof Add) {
					incr = (Const) arithmeticNode.getPred(0);
				} else {
					return null;
				}

				Node startingValue = conditionalPhi.getPred(blockPredecessorLoop == 1 ? 0 : 1);
				if (startingValue == null || !(startingValue instanceof Const))
					return null;

				// get cycle count for loop
				Long cycleCount = getCycleCount(cmp, constant, (Const) startingValue, incr);
				if (cycleCount == null)
					return null;

				System.out.println("start: " + ((Const) startingValue).getTarval().asInt());
				System.out.println("cyclecount: " + cycleCount);

				return new LoopInfo(cycleCount, (Const) startingValue, incr, constant, arithmeticNode,
						conditionalPhi, firstLoopBlock, loopTail, cmp, loopHeader);
			}
		}
		return null;
	}

	/**
	 * Walk from last to first and search all blocks.
	 * 
	 * @param first
	 * @param last
	 * @return
	 */
	public static final Set<Block> getBlocksBetween(Block first, Block last) {
		Set<Block> loopBlocks = new HashSet<>();
		LinkedList<Block> worklist = new LinkedList<>();
		worklist.add(last);
		while (!worklist.isEmpty()) {
			Block block = worklist.pop();
			if (!loopBlocks.contains(block)) {
				loopBlocks.add(block);
				if (!block.equals(first)) {
					for (Node predecessor : block.getPreds()) {
						worklist.add((Block) predecessor.getBlock());
					}
				}
			}
		}
		return loopBlocks;
	}

	public static void removeKeepAlive(Node node) {
		binding_irnode.add_End_keepalive(node.getGraph().getEnd().ptr, node.ptr);
	}

	public static Node getNextPredecessorWithOtherBlock(Node node, int predNum) {
		Node block = node.getBlock();
		while (node.getBlock().equals(block) && node instanceof Phi) {
			node = node.getPred(predNum);
		}
		return node;
	}
}
