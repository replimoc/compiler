package compiler.firm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import compiler.semantic.symbolTable.PrintMethodDefinition;

import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Relation;
import firm.bindings.binding_ircons.op_pin_state;
import firm.nodes.Call;
import firm.nodes.Load;
import firm.nodes.Node;
import firm.nodes.Store;

public class FirmGenerationVisitor implements AstVisitor {

	class State {
		final FirmHierarchy hierarchy;

		// current definitions
		Construction methodConstruction = null;
		int methodVariableCount = 0;
		List<Node> methodReturns = new ArrayList<Node>();

		// create new map for param <-> variable number
		final Map<String, Integer> methodVariables = new HashMap<>();

		String className;

		State(FirmHierarchy hierarchy) {
			this.hierarchy = hierarchy;
		}
	}

	private final State state;
	private firm.nodes.Block trueDestination;
	private firm.nodes.Block falseDestination;

	public FirmGenerationVisitor(FirmHierarchy hierarchy) {
		this.state = new State(hierarchy);
	}

	public FirmGenerationVisitor(State state, firm.nodes.Block trueDestination, firm.nodes.Block falseDestination) {
		this.state = state;
		this.trueDestination = trueDestination;
		this.falseDestination = falseDestination;
	}

	private Node getThisPointer() {
		return state.methodConstruction.getVariable(0, state.hierarchy.getModeRef());
	}

	private String getClassName(Expression expression) {
		return expression.getType().getIdentifier().getValue();
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
				Node cmp = state.methodConstruction.newCmp(operand1, operand2, comparison);
				return state.methodConstruction.newCond(cmp);
			}
		});
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		createFirmForBinaryOperation(additionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return state.methodConstruction.newAdd(operand1, operand2, mode);
			}
		});
	}

	private Node lastRvalueNode = null;

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		// first evaluate rhsExpression, that let lhsExpression decide what to do with rhsExpression;

		Expression rhsExpression = assignmentExpression.getOperand2();
		assert rhsExpression != null;
		rhsExpression.accept(this);

		lastRvalueNode = rhsExpression.getFirmNode();
		Expression lhsExpression = assignmentExpression.getOperand1();
		System.out.println("lhsExpression.getClass() = " + lhsExpression.getClass());
		lhsExpression.accept(this);
		lastRvalueNode = null;
		assignmentExpression.setFirmNode(lhsExpression.getFirmNode());
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		createFirmForBinaryOperation(divisionExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node tupleNode = state.methodConstruction.newDiv(state.methodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
				return state.methodConstruction.newProj(tupleNode, mode, Call.pnTResult);
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
		firm.nodes.Block leftTrueBlock = state.methodConstruction.newBlock();
		evaluateBooleanExpression(logicalAndExpression.getOperand1(), leftTrueBlock, falseDestination);

		shortEvaluationRightOperand(logicalAndExpression, leftTrueBlock);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		firm.nodes.Block leftFalseBlock = state.methodConstruction.newBlock();
		evaluateBooleanExpression(logicalOrExpression.getOperand1(), trueDestination, leftFalseBlock);

		shortEvaluationRightOperand(logicalOrExpression, leftFalseBlock);
	}

	private void shortEvaluationRightOperand(BinaryExpression binaryExpression, firm.nodes.Block block) {
		firm.nodes.Block currentBlock = state.methodConstruction.getCurrentBlock();
		state.methodConstruction.setCurrentBlock(block);
		evaluateBooleanExpression(binaryExpression.getOperand2(), trueDestination, falseDestination);
		state.methodConstruction.setCurrentBlock(currentBlock);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		createFirmForBinaryOperation(moduloExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				Node tupleNode = state.methodConstruction.newMod(state.methodConstruction.getCurrentMem(), operand1, operand2, mode,
						op_pin_state.op_pin_state_pinned);
				return state.methodConstruction.newProj(tupleNode, mode, Call.pnTResult);
			}
		});
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		createFirmForBinaryOperation(multiplicationExpression, new CreateBinaryFirmNode() {
			@Override
			public Node createNode(Node operand1, Node operand2, Mode mode) {
				return state.methodConstruction.newMul(operand1, operand2, mode);
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
				return state.methodConstruction.newSub(operand1, operand2, mode);
			}
		});

	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		boolean boolValue = booleanConstantExpression.isValue();
		int boolIntValue = boolValue ? 1 : 0;

		Node constant = state.methodConstruction.newConst(boolIntValue, state.hierarchy.getModeBool());
		booleanConstantExpression.setFirmNode(constant);
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		// assume that Integer.parseInt doesn't fail (must be checked in semantic analysis)
		String intValue = integerConstantExpression.getIntegerLiteral();
		int val = Integer.parseInt(intValue);

		Node constant = state.methodConstruction.newConst(val, state.hierarchy.getModeInt());
		integerConstantExpression.setFirmNode(constant);
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		boolean isObjThis = false;
		String className;

		if (methodInvocationExpression.getMethodExpression() != null) {
			className = getClassName(methodInvocationExpression.getMethodExpression());
		} else {
			className = state.className;
			isObjThis = true;
		}
		String methodName = methodInvocationExpression.getMethodIdent().getValue();

		System.out.println("className = " + className);
		System.out.println("methodName = " + methodName);

		Node[] paramNodes;
		Entity method;
		MethodType firmMethodType;

		// special case - System.out.println which is PrintStream::println()
		if (methodInvocationExpression.getMethodDefinition() instanceof PrintMethodDefinition) {
			paramNodes = new Node[1];
			methodInvocationExpression.getParameters()[0].accept(this);
			paramNodes[0] = methodInvocationExpression.getParameters()[0].getFirmNode();
			method = state.hierarchy.getPrint_int();
		} else {
			paramNodes = new Node[methodInvocationExpression.getParameters().length + 1];
			// evaluate method object
			Node methodObject;
			if (isObjThis) {
				methodObject = getThisPointer();
			} else {
				methodInvocationExpression.getMethodExpression().accept(this);
				System.out.println("methodInvocationExpression = " + methodInvocationExpression.getMethodExpression());
				methodObject = methodInvocationExpression.getMethodExpression().getFirmNode();
				System.out.println("methodObject = " + methodObject);
			}
			System.out.println("methodExpression = " + methodObject);
			paramNodes[0] = methodObject;

			// evaluate method parameters
			for (int j = 0; j < methodInvocationExpression.getParameters().length; j++) {
				Expression paramExpression = methodInvocationExpression.getParameters()[j];
				paramExpression.accept(this);
				paramNodes[j + 1] = paramExpression.getFirmNode();
				System.out.println("paramNode = " + paramNodes[j + 1]);
			}
			// get method entity
			method = state.hierarchy.getMethodEntity(className, methodName);
		}

		firmMethodType = (MethodType) method.getType();

		// call method
		Node addrOfMethod = state.methodConstruction.newAddress(method);
		Node methodCall = state.methodConstruction.newCall(state.methodConstruction.getCurrentMem(),
				addrOfMethod, paramNodes, firmMethodType);
		Node memAfterCall = state.methodConstruction.newProj(methodCall, Mode.getM(), Call.pnM);
		state.methodConstruction.setCurrentMem(memAfterCall);

		// get result
		if (firmMethodType.getNRess() == 0) {
			methodInvocationExpression.setFirmNode(null);
		} else {
			Node methodResult = state.methodConstruction.newProj(methodCall, Mode.getT(), Call.pnTResult);
			Node resultValue = state.methodConstruction.newProj(methodResult, firmMethodType.getResType(0).getMode(), 0);
			methodInvocationExpression.setFirmNode(resultValue);
		}
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		firm.Type elementsType = state.hierarchy.getType(newArrayExpression.getType().getSubType());

		Expression elementsCount = newArrayExpression.getFirstDimension();
		elementsCount.accept(this);

		Node numberOfElements = elementsCount.getFirmNode();
		assert numberOfElements != null;
		Node sizeofClass = state.methodConstruction.newSize(state.hierarchy.getModeInt(), elementsType);
		Node callocClass = state.methodConstruction.newCall(
				state.methodConstruction.getCurrentMem(),
				state.methodConstruction.newAddress(state.hierarchy.getCalloc()),
				new Node[] { numberOfElements, sizeofClass }, state.hierarchy.getCalloc().getType());
		// update memory
		state.methodConstruction.setCurrentMem(state.methodConstruction.newProj(callocClass, Mode.getM(), Call.pnM));
		// set FirmNode to returned reference
		Node callocResult = state.methodConstruction.newProj(callocClass, Mode.getT(), Call.pnTResult);
		Node referenceToObject = state.methodConstruction.newProj(callocResult, state.hierarchy.getModeRef(), 0);
		newArrayExpression.setFirmNode(referenceToObject);

	}

	private Node intToNode(int i) {
		return state.methodConstruction.newConst(i, state.hierarchy.getModeInt());
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		firm.ClassType classType = state.hierarchy.getClassEntity(getClassName(newObjectExpression));

		// TODO: Alignment?
		Node callocSpace = state.methodConstruction.newCall(state.methodConstruction.getCurrentMem(),
				state.methodConstruction.newAddress(state.hierarchy.getCalloc()),
				new Node[] { intToNode(1), intToNode(classType.getSizeBytes()) }, state.hierarchy.getCalloc().getType());

		// update memory
		state.methodConstruction.setCurrentMem(state.methodConstruction.newProj(callocSpace, Mode.getM(), Call.pnM));
		// set FirmNode to returned reference
		Node callocResult = state.methodConstruction.newProj(callocSpace, Mode.getT(), Call.pnTResult);
		Node referenceToObject = state.methodConstruction.newProj(callocResult, state.hierarchy.getModeRef(), 0);
		newObjectExpression.setFirmNode(referenceToObject);
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		Expression objectNameForFieldAccess = variableAccessExpression.getExpression();
		if (objectNameForFieldAccess == null) {
			String variableName = variableAccessExpression.getFieldIdentifier().getValue();
			if (state.methodVariables.containsKey(variableName)) {
				variableAccess(variableAccessExpression, variableName);
			} else {
				memberAccess(variableAccessExpression, state.className, getThisPointer());
			}
		} else {
			objectNameForFieldAccess.accept(this);
			memberAccess(variableAccessExpression, getClassName(objectNameForFieldAccess), objectNameForFieldAccess.getFirmNode());
		}
	}

	private void variableAccess(VariableAccessExpression variableAccessExpression, String variableName) {
		int variableNumber = state.methodVariables.get(variableName);

		if (lastRvalueNode != null) {
			// this is variable set expression:
			state.methodConstruction.setVariable(variableNumber, lastRvalueNode);
			variableAccessExpression.setFirmNode(lastRvalueNode);
		} else {
			Type astType = variableAccessExpression.getDefinition().getType();
			Mode accessMode = convertAstTypeToMode(astType);
			Node node = state.methodConstruction.getVariable(variableNumber, accessMode);
			variableAccessExpression.setFirmNode(node);
		}
	}

	private void memberAccess(VariableAccessExpression variableAccessExpression, String objectClassName, Node object) {
		Entity field = state.hierarchy.getFieldEntity(objectClassName, variableAccessExpression.getFieldIdentifier().getValue());

		// save rvalue so that field access expression doesn't think it is an assignment
		Node lastRvalueNode = this.lastRvalueNode;
		this.lastRvalueNode = null;

		Node addressOfField = state.methodConstruction.newMember(object, field);

		if (lastRvalueNode != null) {
			Node storeValue = state.methodConstruction.newStore(state.methodConstruction.getCurrentMem(), addressOfField,
					lastRvalueNode);
			Node memoryAfterStore = state.methodConstruction.newProj(storeValue, Mode.getM(), Store.pnM);
			state.methodConstruction.setCurrentMem(memoryAfterStore);
			variableAccessExpression.setFirmNode(lastRvalueNode);
		} else {
			Mode fieldAccessMode = field.getType().getMode();
			Node loadValue = state.methodConstruction.newLoad(state.methodConstruction.getCurrentMem(), addressOfField,
					fieldAccessMode);
			Node loadMememory = state.methodConstruction.newProj(loadValue, Mode.getM(), Load.pnM);
			state.methodConstruction.setCurrentMem(loadMememory);
			Node loadResult = state.methodConstruction.newProj(loadValue, fieldAccessMode, Load.pnRes);
			variableAccessExpression.setFirmNode(loadResult);
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
		firm.Type arrayType = state.hierarchy.getType(arrayAccessExpression.getArrayExpression().getType());

		// calculate index offset
		Node arrayIndex = state.methodConstruction.newSel(refToArray, arrayIndexExpression, arrayType);

		if (lastRvalueNode != null) {
			// we have assignment
			Node storeElement = state.methodConstruction.newStore(state.methodConstruction.getCurrentMem(), arrayIndex, lastRvalueNode);
			Node memAfterStore = state.methodConstruction.newProj(storeElement, Mode.getM(), Store.pnM);
			state.methodConstruction.setCurrentMem(memAfterStore);
			arrayAccessExpression.setFirmNode(memAfterStore);
		} else {
			// we have access
			// load array element and set new memory and result
			Node loadElement = state.methodConstruction.newLoad(
					state.methodConstruction.getCurrentMem(), arrayIndex, arrayElementsMode);
			Node loadMem = state.methodConstruction.newProj(loadElement, Mode.getM(), Load.pnM);
			state.methodConstruction.setCurrentMem(loadMem);
			Node loadResult = state.methodConstruction.newProj(loadElement, arrayElementsMode, Load.pnRes);
			arrayAccessExpression.setFirmNode(loadResult);
		}
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		evaluateBooleanExpression(logicalNotExpression.getOperand(), falseDestination, trueDestination);
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		// get type of expression
		Mode mode = convertAstTypeToMode(negateExpression.getType());

		// get firmNode for the operand
		Expression operand = negateExpression.getOperand();
		operand.accept(this);
		Node operandNode = operand.getFirmNode();

		Node exprNode = state.methodConstruction.newMinus(operandNode, mode);
		negateExpression.setFirmNode(exprNode);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		boolean hasOperand = returnStatement.getOperand() != null;
		Node returnNode;
		if (hasOperand) {
			returnStatement.getOperand().accept(this);
			Node exprNode = returnStatement.getOperand().getFirmNode();
			returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] { exprNode });
		} else {
			// return void
			returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] {});
		}

		state.methodReturns.add(returnNode);
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		thisExpression.setFirmNode(getThisPointer());
	}

	@Override
	public void visit(NullExpression nullExpression) {
		Node cNull = state.methodConstruction.newConst(0, state.hierarchy.getModeRef());
		nullExpression.setFirmNode(cNull);

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

	private Node getConditionNode(Expression expression) {
		// TODO: optimize boolean constants!
		Node conditionNode;
		if (expression.getFirmNode() != null && !expression.getFirmNode().getMode().equals(Mode.getT())) {
			// booleans and boolean constants
			Node trueConst = state.methodConstruction.newConst(1, state.hierarchy.getModeBool());
			Node cmp = state.methodConstruction.newCmp(expression.getFirmNode(), trueConst, Relation.Equal);
			conditionNode = state.methodConstruction.newCond(cmp);
		} else {
			// the result is already the right tuple
			conditionNode = expression.getFirmNode();
		}
		return conditionNode;
	}

	private void evaluateBooleanExpression(Expression condition, firm.nodes.Block trueBlock, firm.nodes.Block falseBlock) {
		FirmGenerationVisitor visitor = new FirmGenerationVisitor(state, trueBlock, falseBlock);
		condition.accept(visitor);

		Node conditionNode = getConditionNode(condition);

		if (conditionNode != null) { // For logical and and logical or this is already done, ignore it.
			Mode mode = Mode.getX();

			Node condTrue = state.methodConstruction.newProj(conditionNode, mode, 1);
			trueBlock.addPred(condTrue);
			Node condFalse = state.methodConstruction.newProj(conditionNode, mode, 0);
			falseBlock.addPred(condFalse);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		boolean hasElseBlock = ifStatement.getFalseCase() != null;

		firm.nodes.Block trueBlock = state.methodConstruction.newBlock();
		firm.nodes.Block falseBlock = state.methodConstruction.newBlock();

		evaluateBooleanExpression(ifStatement.getCondition(), trueBlock, falseBlock);

		falseBlock.mature();
		trueBlock.mature();

		// set new block as active and visit true case
		state.methodConstruction.setCurrentBlock(trueBlock);
		ifStatement.getTrueCase().accept(this);
		Node trueJmp = state.methodConstruction.newJmp();

		// set new block as active and visit false case
		state.methodConstruction.setCurrentBlock(falseBlock);
		if (hasElseBlock) {
			ifStatement.getFalseCase().accept(this);
		}
		Node falseJmp = state.methodConstruction.newJmp();

		// endif block
		firm.nodes.Block endifBlock = state.methodConstruction.newBlock();
		endifBlock.addPred(trueJmp);
		endifBlock.addPred(falseJmp);
		endifBlock.mature();
		state.methodConstruction.setCurrentBlock(endifBlock);

		ifStatement.setFirmNode(null);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		Node startJump = state.methodConstruction.newJmp();

		firm.nodes.Block loopBlock = state.methodConstruction.newBlock();
		firm.nodes.Block endBlock = state.methodConstruction.newBlock();
		firm.nodes.Block startBlock = state.methodConstruction.newBlock();
		startBlock.addPred(startJump);

		state.methodConstruction.setCurrentBlock(startBlock);
		evaluateBooleanExpression(whileStatement.getCondition(), loopBlock, endBlock);
		loopBlock.mature();
		endBlock.mature();

		state.methodConstruction.setCurrentBlock(loopBlock);
		state.methodConstruction.getCurrentMem();

		whileStatement.getBody().accept(this);
		Node loopJump = state.methodConstruction.newJmp();
		startBlock.addPred(loopJump);
		startBlock.mature();

		state.methodConstruction.setCurrentBlock(endBlock);
		state.methodConstruction.getGraph().keepAlive(startBlock);

		whileStatement.setFirmNode(null);
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		int variableNumber = state.methodVariableCount++;
		// add variable number to hash map
		String variableName = localVariableDeclaration.getIdentifier().getValue();
		state.methodVariables.put(variableName, variableNumber);
		System.out.println("variableName = " + variableName);

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			System.out.println("about to visit1 = " + expression.getClass().getName());
			expression.accept(this);

			Node firmNode = expression.getFirmNode();
			assert firmNode != null;
			System.out.println("variableNumber = " + variableNumber);
			state.methodConstruction.setVariable(variableNumber, firmNode);

			// TODO TEMPORARY SET LAST NODE TO VARIABLE ACCESS
			Mode variableMode = convertAstTypeToMode(localVariableDeclaration.getType());
			Node var = state.methodConstruction.getVariable(variableNumber, variableMode);
			localVariableDeclaration.setFirmNode(var);
			// this should be the right one:
			// localVariableDeclaration.setFirmNode(currentMethodConstruction.getCurrentMem());
		} else {
			System.out.println("localVariableDeclaration without assignment");
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		state.className = classDeclaration.getIdentifier().getValue();
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
		clearState();

		Entity methodEntity = state.hierarchy.getMethodEntity(state.className, methodDeclaration.getIdentifier().getValue());
		System.out.println("methodEntity = " + methodEntity);

		int numberLocalVariables = methodDeclaration.getNumberOfLocalVariables();
		int variablesCount = 1 /* this */+ methodDeclaration.getParameters().size() + numberLocalVariables;
		Graph graph = new Graph(methodEntity, variablesCount);
		state.methodConstruction = new Construction(graph);

		Node args = graph.getArgs();
		// set this parameter
		createThisParameter(args);
		// create parameters variables
		for (ParameterDefinition param : methodDeclaration.getParameters()) {
			createParameterDefinition(args, param);
		}

		methodDeclaration.getBlock().accept(this);

		// add all collected return nodes
		for (Node returnNode : state.methodReturns) {
			graph.getEndBlock().addPred(returnNode);
		}
		if (methodDeclaration.getType().getBasicType() == BasicType.VOID) {
			// return void
			Node returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] {});
			graph.getEndBlock().addPred(returnNode);
		}

		state.methodConstruction.setUnreachable();
		state.methodConstruction.finish();
		// clearState map
		state.methodVariables.clear();
	}

	private void createThisParameter(Node args) {
		Node projThis = state.methodConstruction.newProj(args, state.hierarchy.getModeRef(), 0);
		state.methodConstruction.setVariable(0, projThis);
		state.methodVariableCount++;
	}

	private void createParameterDefinition(Node args, ParameterDefinition parameterDefinition) {
		// TODO maybe this is better to do with visitor
		// args can be called as construction.getGraph().getArgs();
		Node paramProj = state.methodConstruction.newProj(args, convertAstTypeToMode(parameterDefinition.getType()),
				state.methodVariableCount++);
		state.methodConstruction.setVariable(state.methodVariableCount, paramProj);
		// add parameter number to map
		state.methodVariables.put(parameterDefinition.getIdentifier().getValue(), state.methodVariableCount);

		parameterDefinition.setFirmNode(paramProj);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		assert "main".equals(staticMethodDeclaration.getIdentifier().getValue());

		clearState();

		int variablesCount = staticMethodDeclaration.getNumberOfLocalVariables();
		System.out.println("num local vars in main = " + variablesCount);
		Graph mainGraph = new Graph(state.hierarchy.getMainMethod(), variablesCount);
		this.state.methodConstruction = new Construction(mainGraph);

		staticMethodDeclaration.getBlock().accept(this);

		Node returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] {});
		mainGraph.getEndBlock().addPred(returnNode);

		state.methodConstruction.setUnreachable();
		state.methodConstruction.finish();
		// clearState map
		state.methodVariables.clear();
	}

	private void clearState() {
		this.lastRvalueNode = null;
		this.state.methodConstruction = null;
		this.state.methodVariables.clear();
		this.state.methodVariableCount = 0;
		this.state.methodReturns.clear();
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
			return state.hierarchy.getModeInt();
		case BOOLEAN:
			return state.hierarchy.getModeBool();
		case CLASS:
		case ARRAY:
			return state.hierarchy.getModeRef();
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
			return state.hierarchy.getModeInt();
		case BOOLEAN:
			return state.hierarchy.getModeBool();
		case CLASS:
		case ARRAY:
			return state.hierarchy.getModeRef();
		default:
			throw new RuntimeException("convertTypeToMode for " + type + " is not implemented");
		}
	}

}
