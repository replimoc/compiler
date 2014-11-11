package compiler.ast.visitor;

import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.StringLiteral;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
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

public interface AstVisitor {
	public void visit(AdditionExpression additionExpression);

	public void visit(AssignmentExpression assignmentExpression);

	public void visit(DivisionExpression divisionExpression);

	public void visit(EqualityExpression equalityExpression);

	public void visit(GreaterThanEqualExpression greaterThanEqualExpression);

	public void visit(GreaterThanExpression greaterThanExpression);

	public void visit(LessThanEqualExpression lessThanEqualExpression);

	public void visit(LessThanExpression lessThanExpression);

	public void visit(LogicalAndExpression logicalAndExpression);

	public void visit(LogicalOrExpression logicalOrExpression);

	public void visit(ModuloExpression moduloExpression);

	public void visit(MuliplicationExpression multiplicationExpression);

	public void visit(NonEqualityExpression nonEqualityExpression);

	public void visit(SubtractionExpression substractionExpression);

	public void visit(BooleanConstantExpression booleanConstantExpression);

	public void visit(IntegerConstantExpression integerConstantExpression);

	public void visit(MethodInvocationExpression methodInvocationExpression);

	public void visit(NewArrayExpression newArrayExpression);

	public void visit(NewObjectExpression newObjectExpression);

	public void visit(VariableAccessExpression variableAccessExpression);

	public void visit(ArrayAccessExpression arrayAccessExpression);

	public void visit(LogicalNotExpression logicalNotExpression);

	public void visit(NegateExpression negateExpression);

	public void visit(ReturnStatement returnStatement);

	public void visit(StringLiteral literal);

	public void visit(ThisExpression thisExpression);

	public void visit(NullExpression nullExpression);

}
