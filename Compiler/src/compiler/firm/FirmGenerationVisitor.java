package compiler.firm;

import compiler.ast.*;
import compiler.ast.statement.*;
import compiler.ast.statement.binary.*;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.nodes.Call;
import firm.nodes.Node;

public class FirmGenerationVisitor implements AstVisitor {

    final FirmHierarchy hierarchy;

    // current definitions
    private Construction currentMethodConstruction = null;
    private int currentMethodVariableCount = 0;

    public FirmGenerationVisitor(FirmHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    @Override
    public void visit(AdditionExpression additionExpression) {

        // get type of expression
        Mode mode = convertAstTypeToMode(additionExpression.getType());

        // get firmNode for fist operand
        Expression operand1 = additionExpression.getOperand1();
        operand1.accept(this);
        Node operand1Node = operand1.getFirmNode();

        // get firmNode for second operand
        Expression operand2 = additionExpression.getOperand2();
        operand2.accept(this);
        Node operand2Node = operand2.getFirmNode();

        // TODO read operand type from expression type
        Node addExpr = currentMethodConstruction.newAdd(operand1Node, operand2Node, mode);
        additionExpression.setFirmNode(addExpr);
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        int variableNumber = -1;
        VariableAccessExpression variableAccess = (VariableAccessExpression) assignmentExpression.getOperand1();
        AstNode variableDefinitionNode = variableAccess.getDefinition().getAstNode();
        // TODO fix this shit
        if (variableDefinitionNode instanceof LocalVariableDeclaration) {
            variableNumber = ((LocalVariableDeclaration) variableDefinitionNode).getFirmVariableNumber();
        }
        Expression rhsExpression = assignmentExpression.getOperand2();
        assert rhsExpression != null; // TODO is this true

        rhsExpression.accept(this);
        Node firmNode = rhsExpression.getFirmNode();
        currentMethodConstruction.setVariable(variableNumber, firmNode);
        assignmentExpression.setFirmNode(currentMethodConstruction.getCurrentMem());
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
        System.out.println("integerConstantExpression = [" + integerConstantExpression + "]");

        // assume that Integer.parseInt doesn't fail (checked in semantic analysis
        String intValue = integerConstantExpression.getIntegerLiteral();
        int val = Integer.parseInt(intValue);

        Node constant = currentMethodConstruction.newConst(val, hierarchy.modeInt);
        integerConstantExpression.setFirmNode(constant);
    }

    @Override
    public void visit(MethodInvocationExpression methodInvocationExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(NewArrayExpression newArrayExpression) {
        firm.Type elementsType = hierarchy.getType(newArrayExpression.getType().getSubType());

        Expression elementsCount = newArrayExpression.getFirstDimension();
        elementsCount.accept(this);

        Node numberOfElements = elementsCount.getFirmNode();
        assert numberOfElements != null;
        Node sizeofClass = currentMethodConstruction.newSize(hierarchy.modeInt, elementsType);
        Node callocClass = currentMethodConstruction.newCall(
                currentMethodConstruction.getCurrentMem(),
                currentMethodConstruction.newAddress(hierarchy.calloc),
                new Node[]{numberOfElements, sizeofClass}, hierarchy.calloc.getType());
        // update memory
        currentMethodConstruction.setCurrentMem(currentMethodConstruction.newProj(callocClass, Mode.getM(), Call.pnM));
        // set FirmNode to returned reference
        Node callocResult = currentMethodConstruction.newProj(callocClass, Mode.getT(), Call.pnTResult);
        Node referenceToObject = currentMethodConstruction.newProj(callocResult, hierarchy.modeRef, 0);
        newArrayExpression.setFirmNode(referenceToObject);

    }

    @Override
    public void visit(NewObjectExpression newObjectExpression) {
        String className = newObjectExpression.getType().getIdentifier().getValue();
        firm.ClassType classType = hierarchy.getClassEntity(className);

        Node numberOfElements = currentMethodConstruction.newConst(1, hierarchy.modeInt);
        Node sizeofClass = currentMethodConstruction.newSize(hierarchy.modeInt, classType);
        Node callocClass = currentMethodConstruction.newCall(
                currentMethodConstruction.getCurrentMem(),
                currentMethodConstruction.newAddress(hierarchy.calloc),
                new Node[]{numberOfElements, sizeofClass}, hierarchy.calloc.getType());
        // update memory
        currentMethodConstruction.setCurrentMem(currentMethodConstruction.newProj(callocClass, Mode.getM(), Call.pnM));
        // set FirmNode to returned reference
        Node callocResult = currentMethodConstruction.newProj(callocClass, Mode.getT(), Call.pnTResult);
        Node referenceToObject = currentMethodConstruction.newProj(callocResult, hierarchy.modeRef, 0);
        newObjectExpression.setFirmNode(referenceToObject);
    }

    @Override
    public void visit(VariableAccessExpression variableAccessExpression) {
        variableAccessExpression.setFirmNode(variableAccessExpression.getDefinition().getAstNode().getFirmNode());
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
        for (Statement statement : block.getStatements()) {
            System.out.println("about to visit: = " + statement.getClass().getName());
            statement.accept(this);
        }

        // get last statement and set block firmNode to this statement
        Statement lastStatement = block.getStatements().get(block.getNumberOfStatements() - 1);
        block.setFirmNode(lastStatement.getFirmNode());
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
        int variableNumber = currentMethodVariableCount++;
        localVariableDeclaration.setFirmVariableNumber(variableNumber);
//        Mode variableMode = convertAstTypeToMode(localVariableDeclaration.getType());

        Expression expression = localVariableDeclaration.getExpression();
        if (expression != null) {
            System.out.println("about to visit = " + expression.getClass().getName());
            expression.accept(this);

            Node firmNode = expression.getFirmNode();
            assert firmNode != null;
            currentMethodConstruction.setVariable(variableNumber, firmNode);
            localVariableDeclaration.setFirmNode(currentMethodConstruction.getCurrentMem());
        } else {
            System.out.println("localVariableDeclaration without assignment");
            System.out.println("localVariableDeclaration = [" + localVariableDeclaration + "]");
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
    public void visit(compiler.ast.Program program) {
        for (ClassDeclaration curr : program.getClasses()) {
            curr.accept(this);
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        Entity methodEntity = hierarchy.getMethodEntity(methodDeclaration.getIdentifier().getValue());
        System.out.println("methodEntity = " + methodEntity);

        currentMethodVariableCount = 0;
        int numberLocalVariables = methodDeclaration.getNumberOfLocalVariables();
        int variablesCount = 1 /*this*/ + methodDeclaration.getParameters().size() + numberLocalVariables;
        Graph graph = new Graph(methodEntity, variablesCount);
        currentMethodConstruction = new Construction(graph);

        Node args = graph.getArgs();
        // set this parameter
        createThisParameter(args);
        // create parameters variables
        for (ParameterDefinition param : methodDeclaration.getParameters()) {
            createParameterDefinition(args, param);
        }

        // TODO block
//        methodDeclaration.getBlock().accept(this);

        Node returnNode;

        // TODO temporary code for this week's assignment
        if (methodDeclaration.getType().getBasicType() == BasicType.VOID) {
            returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[]{});
//            returnNode.setPred(0, methodDeclaration.getBlock().getFirmNode());
        } else {
            Mode constMode = convertAstTypeToMode(methodDeclaration.getType());
            Node constRet = currentMethodConstruction.newConst(0, constMode);
            returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[]{constRet});

        }

        graph.getEndBlock().addPred(returnNode);
        currentMethodConstruction.setUnreachable();
        currentMethodConstruction.finish();
    }

    private void createThisParameter(Node args) {
        Node projThis = currentMethodConstruction.newProj(args, hierarchy.modeRef, 0);
        currentMethodConstruction.setVariable(0, projThis);
        currentMethodVariableCount++;
    }

    private void createParameterDefinition(Node args, ParameterDefinition parameterDefinition) {
        //TODO maybe this is better to do with visitor
        // args can be called as construction.getGraph().getArgs();
        Node paramProj = currentMethodConstruction.newProj(args, convertAstTypeToMode(parameterDefinition.getType()), currentMethodVariableCount++);
        currentMethodConstruction.setVariable(currentMethodVariableCount, paramProj);

        parameterDefinition.setFirmNode(paramProj);
    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(StaticMethodDeclaration staticMethodDeclaration) {
        assert "main".equals(staticMethodDeclaration.getIdentifier().getValue());

        int variablesCount = staticMethodDeclaration.getNumberOfLocalVariables();
        Graph mainGraph = new Graph(hierarchy.mainMethod, variablesCount);
        this.currentMethodConstruction = new Construction(mainGraph);

        staticMethodDeclaration.getBlock().accept(this);

        // TODO: here it is necessary to check whether block contains return statements
        // TODO: and if it does, get it, otherwise return "void" as here
        // TODO: (if I understood correctly )if method returns void it is necessary to link last statement with return
        // TODO: otherwise it won't appear in graph
        Node returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[]{});
        if (staticMethodDeclaration.getBlock().getFirmNode() != null) // TODO
            returnNode.setPred(0, staticMethodDeclaration.getBlock().getFirmNode()); // TODO
        mainGraph.getEndBlock().addPred(returnNode);

        currentMethodConstruction.setUnreachable();
        currentMethodConstruction.finish();
    }

    @Override
    public void visit(ClassType classType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ParameterDefinition parameterDefinition) {
        // TODO Auto-generated method stub

    }

    private firm.Mode convertAstTypeToMode(Type type) {
        switch (type.getBasicType()) {
            case INT:
                return hierarchy.modeInt;
            case BOOLEAN:
                return hierarchy.modeBool;
            case CLASS:
            case ARRAY:
                return hierarchy.modeRef;
            default:
                throw new RuntimeException("convertAstTypeToMode for " + type.getIdentifier() + " is not implemented");
        }
    }

}
