package compiler.firm.optimization.visitor;

public interface OptimizationVisitorFactory<T> {
	public OptimizationVisitor<T> create();
}
