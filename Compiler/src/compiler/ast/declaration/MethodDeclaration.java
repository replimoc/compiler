package compiler.ast.declaration;

import java.util.Arrays;
import java.util.List;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.firm.backend.calling.CallingConvention;
import compiler.lexer.Position;

public class MethodDeclaration extends MethodMemberDeclaration {
	private final Block block;
	private int numberOfLocalVariables;
	private static final CallingConvention CALLING_CONVENTION = CallingConvention.OWN;

	public MethodDeclaration(Position position, Symbol identifier, List<ParameterDeclaration> parameters, Type returnType, Block body) {
		super(position, identifier, parameters, returnType);
		this.block = body;
	}

	public MethodDeclaration(Symbol identifier, Type returnType, Block body, ParameterDeclaration... parameters) {
		this(null, identifier, Arrays.asList(parameters), returnType, body);
	}

	public Block getBlock() {
		return block;
	}

	public void setNumberOfRequiredLocals(int numberOfLocals) {
		this.setNumberOfLocalVariables(numberOfLocals);
	}

	public int getNumberOfLocalVariables() {
		return numberOfLocalVariables;
	}

	public void setNumberOfLocalVariables(int numberOfLocalVariables) {
		this.numberOfLocalVariables = numberOfLocalVariables;
	}

	@Override
	public CallingConvention getCallingConvention() {
		return CALLING_CONVENTION;
	}

	@Override
	protected String getAssemblerNamePrefix() {
		return "m$";
	}

	@Override
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}
}
