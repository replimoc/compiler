package compiler.semantic;

import java.util.HashMap;

import compiler.Symbol;
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
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.semantic.symbolTable.SymbolTable;

public class PreNamingAnalysisVisitor implements AstVisitor {

	private HashMap<Symbol, SymbolTable> classTables = new HashMap<>();
	private SymbolTable currentSymbolTable;

	@Override
	public void visit(AdditionExpression additionExpression) {
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
	}

	@Override
	public void visit(NegateExpression negateExpression) {
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
	}

	@Override
	public void visit(ThisExpression thisExpression) {
	}

	@Override
	public void visit(NullExpression nullExpression) {
	}

	@Override
	public void visit(Type type) {
	}

	@Override
	public void visit(Block block) {
	}

	@Override
	public void visit(IfStatement ifStatement) {
	}

	@Override
	public void visit(WhileStatement whileStatement) {
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration curr : program.getClasses()) {
			curr.accept(this);
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentSymbolTable = new SymbolTable();
		currentSymbolTable.enterScope();

		for (ClassMember curr : classDeclaration.getMembers()) {
			curr.accept(this);
		}

		classTables.put(classDeclaration.getIdentifier(), currentSymbolTable);
		currentSymbolTable = null;
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		// methodDeclaration.get
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
	}
}
