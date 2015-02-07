package compiler.firm.backend.registerallocation.ssa;

import compiler.firm.backend.operations.templates.AssemblerOperation;

public interface AssemblerOperationsVisitor {
	void visit(AssemblerOperation operation);
}
