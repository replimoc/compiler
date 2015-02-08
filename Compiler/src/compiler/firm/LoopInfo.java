package compiler.firm;

import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Node;

public class LoopInfo {
	private final long cycleCount;
	private final Const startingValue;
	private final Const incr;
	private final Const constCmp;
	private final Node arithmeticNode;
	private final Node conditionalPhi;
	private final Block firstLoopBlock;
	private Block lastLoopBlock;
	private final Cmp cmp;
	private final Block loopHeader;
	private boolean negative;
	private long change;
	private long border;
	private long start;

	public LoopInfo(long cycleCount, Const startingValue, Const incr, Const constCmp, Node arithmeticNode,
			Node conditionalPhi, Block firstLoopBlock, Block lastLoopBlock, Cmp cmp, Block loopHeader, long start, long border, long change) {
		this.cycleCount = cycleCount; // Also allow MIN_INT
		this.startingValue = startingValue;
		this.incr = incr;
		this.arithmeticNode = arithmeticNode;
		this.conditionalPhi = conditionalPhi;
		this.constCmp = constCmp;
		this.cmp = cmp;
		this.firstLoopBlock = firstLoopBlock;
		this.lastLoopBlock = lastLoopBlock;
		this.loopHeader = loopHeader;
		this.negative = cycleCount < 0;
		this.start = start;
		this.border = border;
		this.change = change;
	}

	public boolean isOneBlockLoop() {
		return this.getFirstLoopBlock().equals(this.getLastLoopBlock());
	}

	public Block getLastLoopBlock() {
		return lastLoopBlock;
	}

	public Block getFirstLoopBlock() {
		return firstLoopBlock;
	}

	public Node getConditionalPhi() {
		return conditionalPhi;
	}

	public Node getArithmeticNode() {
		return arithmeticNode;
	}

	public Const getConstCmp() {
		return constCmp;
	}

	public Const getIncr() {
		return incr;
	}

	public Const getStartingValue() {
		return startingValue;
	}

	public long getCycleCount() {
		return cycleCount;
	}

	public long getSignedCycleCount() {
		return cycleCount * (negative ? -1 : 1);
	}

	public Cmp getCmp() {
		return cmp;
	}

	public Block getLoopHeader() {
		return loopHeader;
	}

	public void setLastLoopBlock(Block lastLoopBlock) {
		this.lastLoopBlock = lastLoopBlock;
	}

	public long getChange() {
		return change;
	}

	public long getBorder() {
		return border;
	}

	public long getStart() {
		return start;
	}
}