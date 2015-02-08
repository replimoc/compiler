package compiler.firm.backend;

import java.util.List;

import firm.nodes.Block;
import firm.nodes.NodeVisitor;
import firm.nodes.Phi;

public interface BulkPhiNodeVisitor extends NodeVisitor {

	void visit(Block block, List<Phi> phis);

	void visit(List<Phi> phis);

}
