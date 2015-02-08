package compiler.firm.optimization.visitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.firm.optimization.evaluation.ProgramDetails;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.nodes.Add;
import firm.nodes.Address;
import firm.nodes.Block;
import firm.nodes.Call;
import firm.nodes.Conv;
import firm.nodes.Minus;
import firm.nodes.Mul;
import firm.nodes.Node;
import firm.nodes.Not;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Shl;
import firm.nodes.Shr;
import firm.nodes.Shrs;
import firm.nodes.Start;
import firm.nodes.Sub;

public class LoopInvariantVisitor extends OptimizationVisitor<Node> {

	public static final OptimizationVisitorFactory<Node> FACTORY(final ProgramDetails programDetails) {
		return new OptimizationVisitorFactory<Node>() {
			@Override
			public OptimizationVisitor<Node> create() {
				return new LoopInvariantVisitor(programDetails);
			}
		};
	}

	private final ProgramDetails programDetails;

	private HashMap<Node, Node> backedges = new HashMap<>();
	private HashMap<Block, Set<Block>> dominators = new HashMap<>();
	private HashMap<Block, Phi> loopPhis = new HashMap<>();
	private HashSet<Block> ifBlocks = new HashSet<>();
	private OptimizationUtils utils;

	public LoopInvariantVisitor(ProgramDetails programDetails) {
		this.programDetails = programDetails;
	}

	@Override
	public HashMap<Node, Node> getLatticeValues() {
		return nodeReplacements;
	}

	@Override
	public void visit(Add add) {
		final Add addNode = getNodeOrReplacement(add);
		Block leftBlock = (Block) getNodeOrReplacement(addNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(addNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return addNode.getGraph().newAdd(newBlock, addNode.getLeft(), addNode.getRight(), addNode.getMode());
			}

		}, addNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Sub sub) {
		final Sub subNode = getNodeOrReplacement(sub);
		Block leftBlock = (Block) getNodeOrReplacement(subNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(subNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return subNode.getGraph().newSub(newBlock, subNode.getLeft(), subNode.getRight(), subNode.getMode());
			}

		}, subNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shl shl) {
		final Shl shlNode = getNodeOrReplacement(shl);
		Block leftBlock = (Block) getNodeOrReplacement(shlNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shlNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shlNode.getGraph().newShl(newBlock, shlNode.getLeft(), shlNode.getRight(), shlNode.getMode());
			}

		}, shlNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shr shr) {
		final Shr shrNode = getNodeOrReplacement(shr);
		Block leftBlock = (Block) getNodeOrReplacement(shrNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shrNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shrNode.getGraph().newShr(newBlock, shrNode.getLeft(), shrNode.getRight(), shrNode.getMode());
			}

		}, shrNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Shrs shrs) {
		final Shrs shrsNode = getNodeOrReplacement(shrs);
		Block leftBlock = (Block) getNodeOrReplacement(shrsNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(shrsNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return shrsNode.getGraph().newShrs(newBlock, shrsNode.getLeft(), shrsNode.getRight(), shrsNode.getMode());
			}

		}, shrsNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Mul mul) {
		final Mul mulNode = getNodeOrReplacement(mul);
		Block leftBlock = (Block) getNodeOrReplacement(mulNode.getLeft()).getBlock();
		Block rightBlock = (Block) getNodeOrReplacement(mulNode.getRight()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return mulNode.getGraph().newMul(newBlock, mulNode.getLeft(), mulNode.getRight(), mulNode.getMode());
			}

		}, mulNode, leftBlock, rightBlock);
	}

	@Override
	public void visit(Minus minus) {
		final Minus minusNode = getNodeOrReplacement(minus);
		Block operandBlock = (Block) getNodeOrReplacement(minusNode.getOp()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return minusNode.getGraph().newMinus(newBlock, minusNode.getOp(), minusNode.getMode());
			}

		}, minusNode, operandBlock);
	}

	@Override
	public void visit(Not not) {
		final Not notNode = getNodeOrReplacement(not);
		Block operandBlock = (Block) getNodeOrReplacement(notNode.getOp()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return notNode.getGraph().newNot(newBlock, notNode.getOp(), notNode.getMode());
			}

		}, notNode, operandBlock);
	}

	@Override
	public void visit(Conv conv) {
		final Conv convNode = getNodeOrReplacement(conv);
		Block operandBlock = (Block) getNodeOrReplacement(convNode.getOp()).getBlock();

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				return convNode.getGraph().newConv(newBlock, convNode.getOp(), convNode.getMode());
			}

		}, convNode, operandBlock);
	}

	@Override
	public void visit(final Call call) {
		final Call callNode = getNodeOrReplacement(call);
		final Address address = (Address) callNode.getPred(1);
		Entity entity = address.getEntity();
		if (programDetails.hasMemUsage(entity)) {
			return;
		}

		Block[] operandBlocks = new Block[callNode.getPredCount() - 2];
		final Node[] parameters = new Node[callNode.getPredCount() - 2];
		for (int i = 2; i < callNode.getPredCount(); i++) {
			parameters[i - 2] = getNodeOrReplacement(callNode.getPred(i));
			operandBlocks[i - 2] = (Block) (parameters[i - 2]).getBlock();
		}

		replaceNodeIfPossible(new NodeFactory() {
			@Override
			public Node copyNode(Block newBlock) {
				Node mem = getMemoryBeforeLoop(callNode);
				if (mem == null)
					return null;
				Set<Node> memsAfterCall = new HashSet<>();
				for (Edge edge : BackEdges.getOuts(mem)) {
					memsAfterCall.add(edge.node);
				}
				Graph graph = callNode.getGraph();
				Node call = graph.newCall(newBlock, mem, address, parameters, callNode.getType());
				Node projM = graph.newProj(call, Mode.getM(), 0);

				for (Node memAfterCall : memsAfterCall) {
					for (int i = 0; i < memAfterCall.getPredCount(); i++) {
						memAfterCall.setPred(i, projM);
					}
				}
				return call;

			}
		}, callNode, operandBlocks);
	}

	private Node getMemoryBeforeLoop(Call callNode) {
		if (backedges.containsKey(callNode.getBlock())) {
			Block loopBlock = (Block) backedges.get(callNode.getBlock());
			if (loopPhis.containsKey(loopBlock)) {
				Phi loopPhi = loopPhis.get(loopBlock);
				return loopPhi.getPred(0);
			}
		}
		return null;
	}

	private void replaceNodeIfPossible(NodeFactory factory, Node node, Block... operandBlocks) {
		List<Block> operandBlocksList = Arrays.asList(operandBlocks);

		if (dominators.size() > 0 && dominators.get(node.getBlock()).size() > 2) {
			Set<Block> doms = dominators.get(node.getBlock());
			if (doms.containsAll(operandBlocksList)) {
				Node pred = utils.getInnerMostLoopHeader((Block) node.getBlock());
				if (pred == null)
					return;
				// do not move nodes inside if's
				if (ifBlocks.contains(node.getBlock()))
					return;

				Block preLoopBlock = (Block) pred.getPred(0).getBlock();
				// do not move nodes over dominator borders
				Set<Block> domBorder = dominators.get(preLoopBlock);
				if (domBorder.containsAll(operandBlocksList)) {
					Node copy = factory.copyNode(preLoopBlock);
					if (copy == null)
						return;
					addReplacement(node, copy);
					if (node instanceof Call) {
						for (Edge backEdge : BackEdges.getOuts(node)) {
							Node followerNode = backEdge.node;
							if (followerNode.getMode().equals(Mode.getM()) && followerNode instanceof Proj) {
								Proj projM = (Proj) followerNode;
								Node copyProj = node.getGraph().newProj(node, projM.getMode(), projM.getNum());
								copyProj.setBlock(preLoopBlock);
								addReplacement(projM, copyProj);

							} else if (followerNode.getMode().equals(Mode.getT()) && followerNode instanceof Proj) {
								Proj projT = (Proj) followerNode;
								Node copyT = node.getGraph().newProj(copy, projT.getMode(), projT.getNum());
								addReplacement(projT, copyT);
								for (Edge projEdge : BackEdges.getOuts(projT)) {
									// proj nodes after proj T
									Proj proj = (Proj) projEdge.node;
									Node copyProj = node.getGraph().newProj(copyT, proj.getMode(), proj.getNum());
									addReplacement(proj, copyProj);
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNodeOrReplacement(T node) {
		return (T) (nodeReplacements.containsKey(node) ? nodeReplacements.get(node) : node);
	}

	@Override
	public void visit(Start start) {
		utils = new OptimizationUtils(start.getGraph());
		dominators = utils.getDominators();
		backedges = utils.getBackEdges();
		ifBlocks = utils.getIfBlocks();
	}

	private static interface NodeFactory {
		Node copyNode(Block newBlock);
	}
}
