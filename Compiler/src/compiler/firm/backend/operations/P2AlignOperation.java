package compiler.firm.backend.operations;

public class P2AlignOperation extends AssemblerOperation {

	@Override
	public String toString() {
		return "\t.p2align 4,,15\n";
	}

}
