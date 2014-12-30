package compiler.firm.backend;

import java.util.List;

import firm.nodes.NodeVisitor;
import firm.nodes.Phi;

public interface BulkPhiNodeVisitor extends NodeVisitor {

	void visit(List<Phi> phis);

}
