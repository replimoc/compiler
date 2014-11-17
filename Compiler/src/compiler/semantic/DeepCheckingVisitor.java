package compiler.semantic;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
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
import compiler.ast.statement.type.Type;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.visitor.AstVisitor;

public class DeepCheckingVisitor implements AstVisitor {

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

}
