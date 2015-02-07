package compiler.firm;

import firm.nodes.Block;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Node;

public class LoopInfo {
	private final int cycleCount;
	private final Const startingValue;
	private final Const incr;
	private final Const constCmp;
	private final Node arithmeticNode;
	private final Node conditionalPhi;
	private final Block firstLoopBlock;
	private final Block lastLoopBlock;
	private final Cmp cmp;
	private final Block loopHeader;

	public LoopInfo(int cycleCount, Const startingValue, Const incr, Const constCmp, Node arithmeticNode,
			Node conditionalPhi, Block firstLoopBlock, Block lastLoopBlock, Cmp cmp, Block loopHeader) {
		this.cycleCount = cycleCount;
		this.startingValue = startingValue;
		this.incr = incr;
		this.arithmeticNode = arithmeticNode;
		this.conditionalPhi = conditionalPhi;
		this.constCmp = constCmp;
		this.cmp = cmp;
		this.firstLoopBlock = firstLoopBlock;
		this.lastLoopBlock = lastLoopBlock;
		this.loopHeader = loopHeader;
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

	public int getCycleCount() {
		return cycleCount;
	}

	public Cmp getCmp() {
		return cmp;
	}

	public Block getLoopHeader() {
		return loopHeader;
	}

}