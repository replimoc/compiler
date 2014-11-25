package compiler.firm;

import java.util.List;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.DivisionExpression;
import compiler.ast.statement.binary.EqualityExpression;
import compiler.ast.statement.binary.GreaterThanEqualExpression;
import compiler.ast.statement.binary.GreaterThanExpression;
import compiler.ast.statement.binary.LessThanEqualExpression;
import compiler.ast.statement.binary.LessThanExpression;
import compiler.ast.statement.binary.LogicalAndExpression;
import compiler.ast.statement.binary.LogicalOrExpression;
import compiler.ast.statement.binary.ModuloExpression;
import compiler.ast.statement.binary.MuliplicationExpression;
import compiler.ast.statement.binary.NonEqualityExpression;
import compiler.ast.statement.binary.SubtractionExpression;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;

import firm.ArrayType;
import firm.MethodType;
import firm.Mode;
import firm.Mode.Arithmetic;
import firm.PrimitiveType;

public class FirmGenerationVisitor implements AstVisitor {

	// 64 bit pointer
	private final Mode reference;

	public FirmGenerationVisitor() {
		reference = Mode.createReferenceMode("P64", Arithmetic.None, 64, 0);
	}

	private firm.Type createType(Type type) {
		Type tmpType = type;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}

		firm.Type firmType = null;
		switch (tmpType.getBasicType()) {
		case INT:
			firmType = new PrimitiveType(Mode.getIs()); // integer signed 32 bit
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(Mode.getBu()); // unsigned 8 bit
			break;
		case VOID:
			// TODO: do nothing?
			break;
		case NULL:
			// TODO: necessary?
			firmType = new PrimitiveType(reference);
			break;
		}

		if (type.getBasicType() == BasicType.ARRAY) {
			// create composite type
			firmType = new ArrayType(firmType);
		}

		return firmType;
	}

	private firm.Type createType(FieldDeclaration decl) {
		firm.Type type = createType(decl.getType());

		if (type == null) {
			type = new firm.ClassType(decl.getType().getIdentifier().getValue());
		}
		return type;
	}

	private firm.Type createType(ClassMember decl) {
		// TODO:
		throw new RuntimeException();
	}

	private MethodType createType(MethodDeclaration decl) {
		List<ParameterDefinition> params = decl.getParameters();
		firm.Type[] paramTypes = new firm.Type[params.size()];
		for (int i = 0; i < params.size(); i++) {
			paramTypes[i] = createType(params.get(i).getType());
		}

		firm.Type returnType = createType(decl.getType());

		return new MethodType(paramTypes, new firm.Type[] { returnType });
	}

	private firm.ClassType createClassType(ClassDeclaration decl) {
		firm.ClassType classType = new firm.ClassType(decl.getIdentifier().getValue());

		List<ClassMember> members = decl.getMembers();

		for (ClassMember member : members) {
			// TODO: entities?
		}

		return null;
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NegateExpression negateExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ThisExpression thisExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NullExpression nullExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Type type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Block block) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IfStatement ifStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhileStatement whileStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Program program) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ClassType classType) {
		// TODO Auto-generated method stub

	}

}
