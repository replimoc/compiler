package compiler.firm;

import java.util.HashMap;
import java.util.Map;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.MethodInvocationExpression;
import compiler.ast.statement.NewArrayExpression;
import compiler.ast.statement.NewObjectExpression;
import compiler.ast.statement.NullExpression;
import compiler.ast.statement.Statement;
import compiler.ast.statement.ThisExpression;
import compiler.ast.statement.VariableAccessExpression;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.binary.AdditionExpression;
import compiler.ast.statement.binary.AssignmentExpression;
import compiler.ast.statement.binary.BinaryExpression;
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

import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.Mode;
import firm.Relation;
import firm.bindings.binding_ircons.op_pin_state;
import firm.nodes.Call;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Store;

public class FirmGenerationVisitor implements AstVisitor {

	private final FirmHierarchy hierarchy;

	// current definitions
	private Construction currentMethodConstruction = null;
	private int currentMethodVariableCount = 0;
	private final Map<String, Integer> currentMethodVariables;
	// private boolean lValue;

	private String currentClassName;

	public FirmGenerationVisitor(FirmHierarchy hierarchy) {
		this.hierarchy = hierarchy;
		// create new map for param <-> variable number
		currentMethodVariables = new HashMap<String, Integer>();
	}

	private static interface CreateBinaryFirmNode {
		public Node createNode(Node operand1, Node operand2, Mode mode);
	}

	private void createFirmForBinaryOperation(BinaryExpression binaryExpression, CreateBinaryFirmNode firmNodeCreator) {
		// get type of expression
		Mode mode = convertAstTypeToMode(binaryExpression.getType());

		// get firmNode for fist operand
		Expression operand1 = binaryExpression.getOperand1();
		operand1.accept(this);
		Node operand1Node = operand1.getFirmNode();

		// get firmNode for second operand
		Expression operand2 = binaryExpression.getOperand2();
		operand2.accept(this);
		Node operand2Node = operand2.getFirmNode();

		Node exprNode = firmNodeCreator.createNode(operand1Node, operand2Node, mode);
		binaryExpression.setFirmNode(exprNode);
	}

	private void createFirmForComparisonOperation(BinaryExpression binaryExpression, final Relation comparison) {
		createFirmForBinaryOperation(binaryExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node cmp = currentMethodConstruction.newCmp(operand1, operand2, comparison);
				return currentMethodConstruction.newCond(cmp);
			}
		});
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		createFirmForBinaryOperation(additionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return currentMethodConstruction.newAdd(operand1, operand2, mode);
			}
		});
	}

	private Node lastRvalueNode = null;

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		// first evaluate rhsExpression, that let lhsExpression decide what to do with rhsExpression;

		Expression rhsExpression = assignmentExpression.getOperand2();
		assert rhsExpression != null; // TODO is this true
		rhsExpression.accept(this);

		lastRvalueNode = rhsExpression.getFirmNode();
		Expression lhsExpression = assignmentExpression.getOperand1();
		System.out.println("lhsExpression.getClass() = " + lhsExpression.getClass());
		lhsExpression.accept(this);
		assignmentExpression.setFirmNode(currentMethodConstruction.getCurrentMem());
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		createFirmForBinaryOperation(divisionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return currentMethodConstruction.newDiv(currentMethodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
			}
		});
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		createFirmForComparisonOperation(equalityExpression, Relation.Equal);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		createFirmForComparisonOperation(greaterThanEqualExpression, Relation.GreaterEqual);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		createFirmForComparisonOperation(greaterThanExpression, Relation.Greater);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		createFirmForComparisonOperation(lessThanEqualExpression, Relation.LessEqual);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		createFirmForComparisonOperation(lessThanExpression, Relation.Less);
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
		createFirmForBinaryOperation(moduloExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return currentMethodConstruction.newMod(currentMethodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
			}
		});
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		createFirmForBinaryOperation(multiplicationExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return currentMethodConstruction.newMul(operand1, operand2, mode);
			}
		});
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		createFirmForComparisonOperation(nonEqualityExpression, Relation.LessGreater);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		createFirmForBinaryOperation(substractionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return currentMethodConstruction.newSub(operand1, operand2, mode);
			}
		});

	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		boolean boolValue = booleanConstantExpression.isValue();
		int boolIntValue = boolValue ? 1 : 0;

		Node constant = currentMethodConstruction.newConst(boolIntValue, hierarchy.getModeBool());
		booleanConstantExpression.setFirmNode(constant);
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		// assume that Integer.parseInt doesn't fail (must be checked in semantic analysis)
		String intValue = integerConstantExpression.getIntegerLiteral();
		int val = Integer.parseInt(intValue);

		Node constant = currentMethodConstruction.newConst(val, hierarchy.getModeInt());
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
		Node sizeofClass = currentMethodConstruction.newSize(hierarchy.getModeInt(), elementsType);
		Node callocClass = currentMethodConstruction.newCall(
				currentMethodConstruction.getCurrentMem(),
				currentMethodConstruction.newAddress(hierarchy.getCalloc()),
				new Node[] { numberOfElements, sizeofClass }, hierarchy.getCalloc().getType());
		// update memory
		currentMethodConstruction.setCurrentMem(currentMethodConstruction.newProj(callocClass, Mode.getM(), Call.pnM));
		// set FirmNode to returned reference
		Node callocResult = currentMethodConstruction.newProj(callocClass, Mode.getT(), Call.pnTResult);
		Node referenceToObject = currentMethodConstruction.newProj(callocResult, hierarchy.getModeRef(), 0);
		newArrayExpression.setFirmNode(referenceToObject);

	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		String className = newObjectExpression.getType().getIdentifier().getValue();
		firm.ClassType classType = hierarchy.getClassEntity(className);

		Node numberOfElements = currentMethodConstruction.newConst(1, hierarchy.getModeInt());
		Node sizeofClass = currentMethodConstruction.newSize(hierarchy.getModeInt(), classType);
		Node callocClass = currentMethodConstruction.newCall(
				currentMethodConstruction.getCurrentMem(),
				currentMethodConstruction.newAddress(hierarchy.getCalloc()),
				new Node[] { numberOfElements, sizeofClass }, hierarchy.getCalloc().getType());
		// update memory
		currentMethodConstruction.setCurrentMem(currentMethodConstruction.newProj(callocClass, Mode.getM(), Call.pnM));
		// set FirmNode to returned reference
		Node callocResult = currentMethodConstruction.newProj(callocClass, Mode.getT(), Call.pnTResult);
		Node referenceToObject = currentMethodConstruction.newProj(callocResult, hierarchy.getModeRef(), 0);
		newObjectExpression.setFirmNode(referenceToObject);
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {

		if (variableAccessExpression.getExpression() != null) {
			// TODO method invocation or field access or array access;
		} else {
			String variableName = variableAccessExpression.getFieldIdentifier().getValue();
			if (currentMethodVariables.containsKey(variableName)) {
				int variableNumber = currentMethodVariables.get(variableName);

				if (lastRvalueNode != null)
				{
					// this is variable set expression:
					currentMethodConstruction.setVariable(variableNumber, lastRvalueNode);
					lastRvalueNode = null;
					// TODO set node to variable access or current mem?
				}

				Type astType = variableAccessExpression.getDefinition().getType();
				Mode accessMode = convertAstTypeToMode(astType);
				Node node = currentMethodConstruction.getVariable(variableNumber, accessMode);
				variableAccessExpression.setFirmNode(node);
			} else {
				// TODO access to this.field
			}
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// save rvalue so that variable access expression doesn't think it is a store
		Node lastRvalueNode = this.lastRvalueNode;
		this.lastRvalueNode = null;

		// load array variable
		arrayAccessExpression.getArrayExpression().accept(this);
		Node refToArray = arrayAccessExpression.getArrayExpression().getFirmNode();
		System.out.println("refToArray = " + refToArray);
		// load array index
		arrayAccessExpression.getIndexExpression().accept(this);
		Node arrayIndexExpression = arrayAccessExpression.getIndexExpression().getFirmNode();

		// ask developers of firm about this line
		firm.Mode arrayElementsMode = convertAstArrayTypeToElementMode(arrayAccessExpression.getArrayExpression().getType());
		firm.Type arrayType = hierarchy.getType(arrayAccessExpression.getArrayExpression().getType());

		// calculate index offset
		Node arrayIndex = currentMethodConstruction.newSel(refToArray, arrayIndexExpression, arrayType);

		if (lastRvalueNode != null)
		{
			// we have assignment
			Node storeElement = currentMethodConstruction.newStore(currentMethodConstruction.getCurrentMem(), arrayIndex, lastRvalueNode);
			Node memAfterStore = currentMethodConstruction.newProj(storeElement, Mode.getM(), Store.pnM);
			currentMethodConstruction.setCurrentMem(memAfterStore);
		} else {
			// we have access
			// load array element and set new memory and result
			Node loadElement = currentMethodConstruction.newLoad(
					currentMethodConstruction.newNoMem(), arrayIndex, arrayElementsMode);
			Node loadMem = currentMethodConstruction.newProj(loadElement, Mode.getM(), Load.pnM);
			// currentMethodConstruction.setCurrentMem(loadMem);
			Node loadResult = currentMethodConstruction.newProj(loadElement, arrayElementsMode, Load.pnRes);
			arrayAccessExpression.setFirmNode(loadResult);
		}

	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NegateExpression negateExpression) {
		// get type of expression
		Mode mode = convertAstTypeToMode(negateExpression.getType());

		// get firmNode for the operand
		Expression operand = negateExpression.getOperand();
		operand.accept(this);
		Node operandNode = operand.getFirmNode();

		Node exprNode = currentMethodConstruction.newMinus(operandNode, mode);
		negateExpression.setFirmNode(exprNode);
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
		if (!block.isEmpty()) {
			for (Statement statement : block.getStatements()) {
				System.out.println("about to visit: = " + statement.getClass().getName());
				statement.accept(this);
			}

			// get last statement and set block firmNode to this statement
			// FIXME A block can be empty, for example empty main!
			Statement lastStatement = block.getStatements().get(block.getNumberOfStatements() - 1);
			block.setFirmNode(lastStatement.getFirmNode());
		}
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
		// add variable number to hash map
		String variableName = localVariableDeclaration.getIdentifier().getValue();
		currentMethodVariables.put(variableName, variableNumber);
		System.out.println("variableName = " + variableName);

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			System.out.println("about to visit = " + expression.getClass().getName());
			expression.accept(this);

			Node firmNode = expression.getFirmNode();
			assert firmNode != null;
			currentMethodConstruction.setVariable(variableNumber, firmNode);

			// TODO TEMPORARY SET LAST NODE TO VARIABLE ACCESS
			Mode variableMode = convertAstTypeToMode(localVariableDeclaration.getType());
			Node var = currentMethodConstruction.getVariable(variableNumber, variableMode);
			localVariableDeclaration.setFirmNode(var);
			// this should be the right one:
			// localVariableDeclaration.setFirmNode(currentMethodConstruction.getCurrentMem());
		} else {
			System.out.println("localVariableDeclaration without assignment");
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentClassName = classDeclaration.getIdentifier().getValue();
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
		Entity methodEntity = hierarchy.getMethodEntity(currentClassName, methodDeclaration.getIdentifier().getValue());
		System.out.println("methodEntity = " + methodEntity);

		currentMethodVariableCount = 0;
		int numberLocalVariables = methodDeclaration.getNumberOfLocalVariables();
		int variablesCount = 1 /* this */+ methodDeclaration.getParameters().size() + numberLocalVariables;
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
		// methodDeclaration.getBlock().accept(this);

		Node returnNode;

		// TODO temporary code for this week's assignment
		if (methodDeclaration.getType().getBasicType() == BasicType.VOID) {
			returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[] {});
			// returnNode.setPred(0, methodDeclaration.getBlock().getFirmNode());
		} else {
			Mode constMode = convertAstTypeToMode(methodDeclaration.getType());
			Node constRet = currentMethodConstruction.newConst(0, constMode);
			returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[] { constRet });

		}

		graph.getEndBlock().addPred(returnNode);
		currentMethodConstruction.setUnreachable();
		currentMethodConstruction.finish();
		// clear map
		currentMethodVariables.clear();
	}

	private void createThisParameter(Node args) {
		Node projThis = currentMethodConstruction.newProj(args, hierarchy.getModeRef(), 0);
		currentMethodConstruction.setVariable(0, projThis);
		currentMethodVariableCount++;
	}

	private void createParameterDefinition(Node args, ParameterDefinition parameterDefinition) {
		// TODO maybe this is better to do with visitor
		// args can be called as construction.getGraph().getArgs();
		Node paramProj = currentMethodConstruction.newProj(args, convertAstTypeToMode(parameterDefinition.getType()), currentMethodVariableCount++);
		currentMethodConstruction.setVariable(currentMethodVariableCount, paramProj);
		// add parameter number to map
		currentMethodVariables.put(parameterDefinition.getIdentifier().getValue(), currentMethodVariableCount);

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
		Graph mainGraph = new Graph(hierarchy.getMainMethod(), variablesCount);
		this.currentMethodConstruction = new Construction(mainGraph);

		staticMethodDeclaration.getBlock().accept(this);

		// TODO: here it is necessary to check whether block contains return statements
		// TODO: and if it does, get it, otherwise return "void" as here
		// TODO: (if I understood correctly )if method returns void it is necessary to link last statement with return
		// TODO: otherwise it won't appear in graph
		Node returnNode = currentMethodConstruction.newReturn(currentMethodConstruction.getCurrentMem(), new Node[] {});
		if (staticMethodDeclaration.getBlock().getFirmNode() != null) // TODO
			returnNode.setPred(0, staticMethodDeclaration.getBlock().getFirmNode()); // TODO
		mainGraph.getEndBlock().addPred(returnNode);

		currentMethodConstruction.setUnreachable();
		currentMethodConstruction.finish();
		// clear map
		currentMethodVariables.clear();
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
			return hierarchy.getModeInt();
		case BOOLEAN:
			return hierarchy.getModeBool();
		case CLASS:
		case ARRAY:
			return hierarchy.getModeRef();
		default:
			throw new RuntimeException("convertTypeToMode for " + type + " is not implemented");
		}
	}

	private firm.Mode convertAstArrayTypeToElementMode(Type type) {
		compiler.ast.type.Type tmpType = type;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}

		switch (tmpType.getBasicType()) {
		case INT:
			return hierarchy.getModeInt();
		case BOOLEAN:
			return hierarchy.getModeBool();
		case CLASS:
		case ARRAY:
			return hierarchy.getModeRef();
		default:
			throw new RuntimeException("convertTypeToMode for " + type + " is not implemented");
		}
	}

}
