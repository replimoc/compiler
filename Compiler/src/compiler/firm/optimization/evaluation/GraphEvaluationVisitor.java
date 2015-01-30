package compiler.firm.optimization.evaluation;

import java.util.HashSet;

import compiler.firm.optimization.AbstractFirmNodesVisitor;

import firm.BackEdges;
import firm.BackEdges.Edge;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.nodes.Address;
import firm.nodes.Call;
import firm.nodes.Cmp;
import firm.nodes.Const;
import firm.nodes.Div;
import firm.nodes.Jmp;
import firm.nodes.Load;
import firm.nodes.Mod;
import firm.nodes.Node;
import firm.nodes.Phi;
import firm.nodes.Proj;
import firm.nodes.Return;
import firm.nodes.Store;

public class GraphEvaluationVisitor extends AbstractFirmNodesVisitor {

	public static void calculateStaticDetails(Graph graph, EntityDetails details) {
		HashSet<Integer> unusedParameters = new HashSet<Integer>();

		for (Edge startFollower : BackEdges.getOuts(graph.getStart())) {
			Node startFollowerNode = startFollower.node;

			if (startFollowerNode instanceof Proj && startFollowerNode.getMode().equals(Mode.getT())) {
				MethodType methodType = (MethodType) graph.getEntity().getType();
				int numberOfParams = methodType.getNParams();
				for (int i = 0; i < numberOfParams; i++) {
					unusedParameters.add(i);
				}

				for (Edge parameterEdge : BackEdges.getOuts(startFollowerNode)) {
					Node parameterNode = parameterEdge.node;
					if (parameterNode instanceof Proj) {
						unusedParameters.remove(((Proj) parameterNode).getNum());
					}
				}
			}
		}
		details.setUnusedParameters(unusedParameters);
	}

	private final ProgramDetails programDetails;
	private final EntityDetails ownDetails;
	private int numberOfNodes = 0;

	public GraphEvaluationVisitor(Graph graph, ProgramDetails programDetails) {
		this.programDetails = programDetails;
		this.ownDetails = programDetails.getEntityDetails(graph);
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	@Override
	public void visit(Call callNode) {
		super.visit(callNode);

		int constantArguments = 0;
		for (int i = 2; i < callNode.getPredCount(); i++) {
			if (callNode.getPred(i) instanceof Const) {
				constantArguments++;
			}
		}

		final Address address = (Address) callNode.getPred(1);
		Entity calledEntity = address.getEntity();
		EntityDetails calledEntityDetails = programDetails.getEntityDetails(calledEntity);

		calledEntityDetails.addCallToEntityInfo(callNode, new CallInformation(callNode.getGraph().getEntity(), constantArguments));
		ownDetails.addCallFromEntityInfo(callNode, new CallInformation(calledEntity, constantArguments));
	}

	private void collectEnd(Node node) {
		EntityDetails entityDetails = programDetails.getEntityDetails(node.getGraph().getEntity());
		entityDetails.addBlockInfo(node.getBlock(), new BlockInformation(node));
	}

	@Override
	public void visit(Cmp cmp) {
		super.visit(cmp);
		collectEnd(cmp);
	}

	@Override
	public void visit(Jmp jmp) {
		super.visit(jmp);
		collectEnd(jmp);
	}

	@Override
	public void visit(Return ret) {
		super.visit(ret);
		collectEnd(ret);
	}

	@Override
	public void visit(Load load) {
		super.visit(load);
		ownDetails.setHasMemUsage();
	}

	@Override
	public void visit(Store store) {
		super.visit(store);
		ownDetails.setHasSideEffects();
		ownDetails.setHasMemUsage();
	}

	@Override
	public void visit(Div div) {
		super.visit(div);
		ownDetails.setHasMemUsage();
	}

	@Override
	public void visit(Mod mod) {
		super.visit(mod);
		ownDetails.setHasMemUsage();
	}

	@Override
	public void visit(Phi phi) {
		super.visit(phi);
		if (phi.getMode().equals(Mode.getM())) {
			ownDetails.setHasSideEffects();
			ownDetails.setHasMemUsage();
		}
	}

	@Override
	protected void visitNode(Node node) {
		numberOfNodes++;
	}
}
