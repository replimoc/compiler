package compiler.firm;

import compiler.ast.*;
import compiler.ast.statement.*;
import compiler.ast.statement.binary.*;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import firm.Entity;

/**
 * Generate hierarchy of Firm Entities (fill FirmHierarchy)
 */
public class FirmHierarchyGenerationVisitor implements AstVisitor {

    final FirmHierarchy hierarchy;


    public FirmHierarchyGenerationVisitor(FirmHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    @Override
    public void visit(Program program) {
        // in a normal program there are subclasses that need to be visited first
        // but since we don't have subclasses we will just create classes
        for (ClassDeclaration classDeclaration : program.getClasses()) {
            hierarchy.addClass(classDeclaration.getIdentifier().getValue());
        }

        // now iterate over fields and methods and create them
        for (ClassDeclaration curr : program.getClasses()) {
            curr.accept(this);
        }
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        hierarchy.setCurrentClass(classDeclaration.getIdentifier().getValue());
        for (ClassMember curr : classDeclaration.getMembers()) {
            curr.accept(this);
        }
    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration) {
        hierarchy.addFieldEntity(fieldDeclaration.getType(), fieldDeclaration.getIdentifier().getValue());
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        hierarchy.addMethodEntity(methodDeclaration.getIdentifier().getValue(), methodDeclaration.getParameters(), methodDeclaration.getType());

    }

    @Override
    public void visit(StaticMethodDeclaration staticMethodDeclaration) {
        assert "main".equals(staticMethodDeclaration.getIdentifier().getValue());
    }

    // everything below is not implemented and should never be called

    @Override
    public void visit(AdditionExpression additionExpression) {
        assert false;
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        assert false;
    }

    @Override
    public void visit(DivisionExpression divisionExpression) {
        assert false;
    }

    @Override
    public void visit(EqualityExpression equalityExpression) {
        assert false;
    }

    @Override
    public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
        assert false;
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        assert false;
    }

    @Override
    public void visit(LessThanEqualExpression lessThanEqualExpression) {
        assert false;
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        assert false;
    }

    @Override
    public void visit(LogicalAndExpression logicalAndExpression) {
        assert false;
    }

    @Override
    public void visit(LogicalOrExpression logicalOrExpression) {
        assert false;
    }

    @Override
    public void visit(ModuloExpression moduloExpression) {
        assert false;
    }

    @Override
    public void visit(MuliplicationExpression multiplicationExpression) {
        assert false;
    }

    @Override
    public void visit(NonEqualityExpression nonEqualityExpression) {
        assert false;
    }

    @Override
    public void visit(SubtractionExpression substractionExpression) {
        assert false;
    }

    @Override
    public void visit(BooleanConstantExpression booleanConstantExpression) {
        assert false;
    }

    @Override
    public void visit(IntegerConstantExpression integerConstantExpression) {
        assert false;
    }

    @Override
    public void visit(MethodInvocationExpression methodInvocationExpression) {
        assert false;
    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        assert false;
    }

    @Override
    public void visit(NewObjectExpression newObjectExpression) {
        assert false;
    }

    @Override
    public void visit(VariableAccessExpression variableAccessExpression) {
        assert false;
    }

    @Override
    public void visit(ArrayAccessExpression arrayAccessExpression) {
        assert false;
    }

    @Override
    public void visit(LogicalNotExpression logicalNotExpression) {
        assert false;
    }

    @Override
    public void visit(NegateExpression negateExpression) {
        assert false;
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        assert false;
    }

    @Override
    public void visit(ThisExpression thisExpression) {
        assert false;
    }

    @Override
    public void visit(NullExpression nullExpression) {
        assert false;
    }

    @Override
    public void visit(Type type) {
        assert false;
    }

    @Override
    public void visit(Block block) {
        assert false;
    }

    @Override
    public void visit(IfStatement ifStatement) {
        assert false;
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        assert false;
    }

    @Override
    public void visit(LocalVariableDeclaration localVariableDeclaration) {
        assert false;
    }

    @Override
    public void visit(ParameterDefinition parameterDefinition) {
        assert false;
    }

    @Override
    public void visit(ClassType classType) {
        assert false;
    }

}