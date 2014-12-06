package compiler.firm;

import java.util.HashMap;
import java.util.Map;

import compiler.Symbol;
import compiler.ast.AstNode;
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
import compiler.semantic.symbolTable.Definition;
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
		final Map<firm.nodes.Block, Node> methodReturns = new HashMap<firm.nodes.Block, Node>();

		// create new map for param <-> variable number
		final Map<DefinitionKey, Integer> methodVariables = new HashMap<>();
		Node activePhiNode;
		AstNode activeLocalVariableDeclaration = null;

		String className;

		Expression assignmentRightSide = null;

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

	private Node getCallocAddress() {
		return state.methodConstruction.newAddress(state.hierarchy.getCalloc());
	}

	private static interface CreateBinaryFirmNode {
		public Node createNode(Node operand1, Node operand2, Mode mode);
	}

	private void createFirmForBinaryOperation(BinaryExpression binaryExpression, CreateBinaryFirmNode firmNodeCreator) {
		// get type of expression
		Mode mode = convertAstTypeToMode(binaryExpression.getType());

		// get firmNode for fist operand
		Expression operand1 = binaryExpression.getOperand1();
		Node operand1Node;
		if (operand1.getType().getBasicType() == BasicType.BOOLEAN) {
			// maybe additional blocks need to be created
			operand1Node = createBooleanNodeFromBinaryExpression(operand1);
		} else {
			operand1.accept(this);
			operand1Node = operand1.getFirmNode();
		}
		// get firmNode for second operand
		Expression operand2 = binaryExpression.getOperand2();
		Node operand2Node;
		if (operand1.getType().getBasicType() == BasicType.BOOLEAN) {
			// maybe additional blocks need to be created
			operand2Node = createBooleanNodeFromBinaryExpression(operand2);
		} else {
			operand2.accept(this);
			operand2Node = operand2.getFirmNode();
		}

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

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		// first evaluate rhsExpression, than let lhsExpression decide what to do with rhsExpression;

		Expression leftExpression = assignmentExpression.getOperand1();
		Expression rightExpression = assignmentExpression.getOperand2();
		assert rightExpression != null;
		state.assignmentRightSide = rightExpression;
		leftExpression.accept(this);
		state.assignmentRightSide = null;
		assignmentExpression.setFirmNode(rightExpression.getFirmNode());
	}

	private Node getRightSideOfAssignment(Expression rightExpression) {
		if (rightExpression.getFirmNode() != null) {
			return rightExpression.getFirmNode();
		}

		if (rightExpression.getType().getBasicType() == BasicType.BOOLEAN) {
			return createBooleanNodeFromBinaryExpression(rightExpression);
		} else {
			rightExpression.accept(this);
			return rightExpression.getFirmNode();
		}
	}

	private Node createBooleanNodeFromBinaryExpression(Expression expression) {
		if (expression instanceof BooleanConstantExpression) {
			expression.accept(this);
			return expression.getFirmNode();
		} else {
			firm.nodes.Block trueBlock = state.methodConstruction.newBlock();
			firm.nodes.Block falseBlock = state.methodConstruction.newBlock();
			firm.nodes.Block afterBlock = state.methodConstruction.newBlock();

			evaluateBooleanExpression(expression, trueBlock, falseBlock);
			// create true block assigning true to temp variable
			trueBlock.mature();
			state.methodConstruction.setCurrentBlock(trueBlock);
			Node trueConst = state.methodConstruction.newConst(1, state.hierarchy.getModeBool());
			Node trueJump = state.methodConstruction.newJmp();
			afterBlock.addPred(trueJump);

			// create false block assigning false to temp variable
			falseBlock.mature();
			state.methodConstruction.setCurrentBlock(falseBlock);
			Node falseConst = state.methodConstruction.newConst(0, state.hierarchy.getModeBool());
			Node falseJump = state.methodConstruction.newJmp();
			afterBlock.addPred(falseJump);

			afterBlock.mature();
			state.methodConstruction.setCurrentBlock(afterBlock);
			Node phi = state.methodConstruction.newPhi(new Node[] { trueConst, falseConst }, state.hierarchy.getModeBool());
			state.activePhiNode = phi;
			return phi;
		}
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
		Node constant = createBooleanConstantNode(booleanConstantExpression.isValue());
		booleanConstantExpression.setFirmNode(constant);
	}

	private Node createBooleanConstantNode(boolean boolValue) {
		int boolIntValue = boolValue ? 1 : 0;
		return state.methodConstruction.newConst(boolIntValue, state.hierarchy.getModeBool());
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
		MethodCallInformation methodCallInformation = new MethodCallInformation();

		// special case - System.out.println which is PrintStream::println()
		if (methodInvocationExpression.getMethodDefinition() instanceof PrintMethodDefinition) {
			methodCallInformation = new PrintMethodCallInformation();
		}

		methodCallInformation.generate(methodInvocationExpression, this);
		methodInvocationExpression.setFirmNode(callMethod(methodCallInformation));
	}

	private class MethodCallInformation {
		Node[] parameterNodes;
		Entity method;

		public void generate(MethodInvocationExpression methodInvocationExpression, AstVisitor visitor) {
			String className;
			Node methodObject;

			// Generate method name
			Expression methodExpression = methodInvocationExpression.getMethodExpression();
			if (methodExpression != null) {
				methodExpression.accept(visitor);

				className = getClassName(methodExpression);
				methodObject = methodExpression.getFirmNode();
			} else {
				className = state.className;
				methodObject = getThisPointer();
			}
			String methodName = methodInvocationExpression.getMethodIdent().getValue();
			method = state.hierarchy.getMethodEntity(className, methodName);

			// Generate parameter list
			Expression[] parameters = methodInvocationExpression.getParameters();
			parameterNodes = new Node[parameters.length + 1];
			parameterNodes[0] = methodObject;

			for (int j = 0; j < parameters.length; j++) {
				Expression paramExpression = parameters[j];
				if (paramExpression.getType().getBasicType() == BasicType.BOOLEAN) {
					parameterNodes[j + 1] = createBooleanNodeFromBinaryExpression(paramExpression);
				} else {
					paramExpression.accept(visitor);
					Node paramNode = paramExpression.getFirmNode();
					parameterNodes[j + 1] = paramNode;
				}
			}
		}
	}

	private class PrintMethodCallInformation extends MethodCallInformation {
		@Override
		public void generate(MethodInvocationExpression methodInvocationExpression, AstVisitor visitor) {
			methodInvocationExpression.getParameters()[0].accept(visitor);
			parameterNodes = new Node[1];
			parameterNodes[0] = methodInvocationExpression.getParameters()[0].getFirmNode();
			method = state.hierarchy.getPrint_int();
		}
	}

	private Node callMethod(MethodCallInformation info) {
		MethodType firmMethodType = (MethodType) info.method.getType();

		Node addressOfMethod = state.methodConstruction.newAddress(info.method);

		return callMethod(addressOfMethod, info.parameterNodes, firmMethodType, firmMethodType);
	}

	private Node callMethod(Node addressOfMethod, Node[] parameterNodes, firm.Type type, MethodType firmMethodType) {
		Node methodCall = state.methodConstruction.newCall(state.methodConstruction.getCurrentMem(), addressOfMethod, parameterNodes,
				type);
		Node memoryAfterCall = state.methodConstruction.newProj(methodCall, Mode.getM(), Call.pnM);
		state.methodConstruction.setCurrentMem(memoryAfterCall);

		// get result
		Node resultValue = null;
		if (firmMethodType == null) { // Generate reference, this is an calloc call
			// TODO: I'm note sure, but this is maybe wrong!
			Node methodResult = state.methodConstruction.newProj(methodCall, Mode.getT(), Call.pnTResult);
			resultValue = state.methodConstruction.newProj(methodResult, state.hierarchy.getModeRef(), 0);
		} else if (firmMethodType.getNRess() != 0) {
			Node methodResult = state.methodConstruction.newProj(methodCall, Mode.getT(), Call.pnTResult);
			resultValue = state.methodConstruction.newProj(methodResult, firmMethodType.getResType(0).getMode(), 0);
		}
		return resultValue;
	}

	private Node callCalloc(Node numberOfElements, Node sizeofClass) {
		return callMethod(getCallocAddress(),
				new Node[] { numberOfElements, sizeofClass },
				state.hierarchy.getCalloc().getType(),
				null);
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		int elementsSize = state.hierarchy.getTypeDeclaration(newArrayExpression.getType().getSubType(), true).getSizeBytes();

		Expression elementsCount = newArrayExpression.getFirstDimension();
		elementsCount.accept(this);

		Node referenceToObject = callCalloc(elementsCount.getFirmNode(), intToNode(elementsSize));
		newArrayExpression.setFirmNode(referenceToObject);
	}

	private Node intToNode(int i) {
		return state.methodConstruction.newConst(i, state.hierarchy.getModeInt());
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		firm.ClassType classType = state.hierarchy.getClassEntity(getClassName(newObjectExpression));
		Node referenceToObject = callCalloc(intToNode(1), intToNode(classType.getSizeBytes()));
		newObjectExpression.setFirmNode(referenceToObject);
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		Expression assignmentRightSide = this.state.assignmentRightSide;
		this.state.assignmentRightSide = null;

		Expression objectNameForFieldAccess = variableAccessExpression.getExpression();
		if (objectNameForFieldAccess == null) {
			Definition def = variableAccessExpression.getDefinition();
			if (def == null) {
				// definition not set yet because it is a declaration + assignment
				// e.g. int x = 10;
				def = new Definition(variableAccessExpression.getFieldIdentifier(), state.activeLocalVariableDeclaration.getType(),
						state.activeLocalVariableDeclaration);
			}
			DefinitionKey variableDef = new DefinitionKey(def.getSymbol(), def.getType(), def.getAstNode());
			if (state.methodVariables.containsKey(variableDef)) {
				variableAccess(variableAccessExpression, variableDef, assignmentRightSide);
			} else {
				memberAccess(variableAccessExpression, state.className, getThisPointer(), assignmentRightSide);
			}
		} else {
			objectNameForFieldAccess.accept(this);
			memberAccess(variableAccessExpression, getClassName(objectNameForFieldAccess), objectNameForFieldAccess.getFirmNode(),
					assignmentRightSide);
		}
	}

	private void variableAccess(VariableAccessExpression variableAccessExpression, DefinitionKey variableDef, Expression assignmentRightSide) {
		int variableNumber = state.methodVariables.get(variableDef);
		state.methodConstruction.getCurrentMem();

		if (assignmentRightSide != null) {
			Node rightSideNode = getRightSideOfAssignment(assignmentRightSide);
			// this is variable set expression:
			state.methodConstruction.setVariable(variableNumber, rightSideNode);
			variableAccessExpression.setFirmNode(rightSideNode);
		} else {
			Type astType = variableAccessExpression.getDefinition().getType();
			Mode accessMode = convertAstTypeToMode(astType);
			Node node = state.methodConstruction.getVariable(variableNumber, accessMode);
			variableAccessExpression.setFirmNode(node);
		}
	}

	private void memberAccess(VariableAccessExpression variableAccessExpression, String objectClassName, Node object, Expression assignmentRightSide) {
		String attribute = variableAccessExpression.getFieldIdentifier().getValue();
		Entity field = state.hierarchy.getFieldEntity(objectClassName, attribute);

		Node addressOfField = state.methodConstruction.newMember(object, field);

		if (assignmentRightSide != null) {
			memberAssign(addressOfField, assignmentRightSide);
			variableAccessExpression.setFirmNode(assignmentRightSide.getFirmNode());
		} else {
			firm.Mode fieldAccessMode = field.getType().getMode();
			// primitive types have modes, Pointer types don't and there is no method to set it
			// for minijava it is safe to set all pointers to reference
			if (fieldAccessMode == null)
				fieldAccessMode = state.hierarchy.getModeRef();
			Node member = memberGet(addressOfField, fieldAccessMode);
			variableAccessExpression.setFirmNode(member);
		}
	}

	private Node memberAssign(Node addressOfField, Expression content) {
		Node contentNode = getRightSideOfAssignment(content);
		Node storeValue = state.methodConstruction.newStore(state.methodConstruction.getCurrentMem(), addressOfField, contentNode);
		Node memoryAfterStore = state.methodConstruction.newProj(storeValue, Mode.getM(), Store.pnM);
		state.methodConstruction.setCurrentMem(memoryAfterStore);
		return memoryAfterStore;
	}

	private Node memberGet(Node addressOfField, Mode fieldAccessMode) {
		Node loadValue = state.methodConstruction.newLoad(state.methodConstruction.getCurrentMem(), addressOfField, fieldAccessMode);
		Node loadMememory = state.methodConstruction.newProj(loadValue, Mode.getM(), Load.pnM);
		state.methodConstruction.setCurrentMem(loadMememory);
		return state.methodConstruction.newProj(loadValue, fieldAccessMode, Load.pnRes);
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// save rvalue so that variable access expression doesn't think it is a store
		Expression assignmentRightSide = this.state.assignmentRightSide;
		this.state.assignmentRightSide = null;

		Expression arrayExpression = arrayAccessExpression.getArrayExpression();
		Expression indexExpression = arrayAccessExpression.getIndexExpression();

		arrayExpression.accept(this);
		indexExpression.accept(this);

		// calculate index offset
		Node arrayIndex = state.methodConstruction
				.newSel(arrayExpression.getFirmNode(), indexExpression.getFirmNode(), getArrayType(arrayExpression));

		Node result = null;
		if (assignmentRightSide != null) {
			result = memberAssign(arrayIndex, assignmentRightSide);
		} else {
			result = memberGet(arrayIndex, convertAstTypeToMode(arrayExpression.getType().getSubType()));
		}
		arrayAccessExpression.setFirmNode(result);
	}

	private firm.Type getArrayType(Expression expression) {
		return state.hierarchy.getTypeDeclaration(expression.getType(), false);
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
		// prevent a second return from being set for this block
		if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
			Node returnNode;
			if (hasOperand) {
				returnStatement.getOperand().accept(this);
				Node exprNode = returnStatement.getOperand().getFirmNode();
				returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] { exprNode });
			} else {
				// return void
				returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] {});
			}

			state.methodReturns.put(state.methodConstruction.getCurrentBlock(), returnNode);
		}
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
	public void visit(Block block) {
		if (!block.isEmpty()) {
			for (Statement statement : block.getStatements()) {
				// do not visit blocks which already have a return
				if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
					System.out.println("about to visit: = " + statement.getClass().getName());
					statement.accept(this);
				}
			}

			// get last statement and set block firmNode to this statement
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
		Mode mode = Mode.getX();

		if (conditionNode != null && !conditionNode.getBlock().equals(state.methodConstruction.getCurrentBlock())) {
			// TODO: find a more elegant way to do this!
			// we reference a condition node that is not in the current block, get the last set phi node instead
			// create a new condition node for that phi node
			Node trueConst = state.methodConstruction.newConst(1, state.hierarchy.getModeBool());
			Node cmp = state.methodConstruction.newCmp(state.activePhiNode, trueConst, Relation.Equal);
			conditionNode = state.methodConstruction.newCond(cmp);
			Node condTrue = state.methodConstruction.newProj(conditionNode, mode, 1);
			trueBlock.addPred(condTrue);
			Node condFalse = state.methodConstruction.newProj(conditionNode, mode, 0);
			falseBlock.addPred(condFalse);
			state.activePhiNode = null;
		} else if (conditionNode != null) { // For logical and and logical or this is already done, ignore it.
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
		firm.nodes.Block falseBlock;
		firm.nodes.Block endifBlock = state.methodConstruction.newBlock();

		if (hasElseBlock) {
			// only create new block if necessary
			falseBlock = state.methodConstruction.newBlock();
		} else {
			falseBlock = endifBlock;
		}

		evaluateBooleanExpression(ifStatement.getCondition(), trueBlock, falseBlock);
		trueBlock.mature();

		// set new block as active and visit true case
		state.methodConstruction.setCurrentBlock(trueBlock);
		ifStatement.getTrueCase().accept(this);
		// prevent a second control flow instruction from being set for this block
		if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
			Node trueJmp = state.methodConstruction.newJmp();
			endifBlock.addPred(trueJmp);
		}
		if (hasElseBlock) {
			// only mature if else block is present
			falseBlock.mature();

			// set new block as active and visit false case
			state.methodConstruction.setCurrentBlock(falseBlock);
			ifStatement.getFalseCase().accept(this);
			// prevent a second control flow instruction from being set for this block
			if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
				Node falseJmp = state.methodConstruction.newJmp();
				endifBlock.addPred(falseJmp);
			}
		}
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
		if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
			Node loopJump = state.methodConstruction.newJmp();
			startBlock.addPred(loopJump);
		}
		startBlock.mature();
		state.methodConstruction.setCurrentBlock(endBlock);
		state.methodConstruction.getGraph().keepAlive(startBlock);

		whileStatement.setFirmNode(null);
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		int variableNumber = state.methodVariableCount++;
		// add variable number to hash map
		DefinitionKey variableDef = new DefinitionKey(localVariableDeclaration.getIdentifier(), localVariableDeclaration.getType(),
				localVariableDeclaration);
		state.methodVariables.put(variableDef, variableNumber);
		System.out.println("variableName = " + variableDef + " (" + variableNumber + ")");
		state.activeLocalVariableDeclaration = localVariableDeclaration;

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			AssignmentExpression assignment = new AssignmentExpression(null, new VariableAccessExpression(null, null,
					localVariableDeclaration.getIdentifier()), expression);
			assignment.accept(this);
		} else {
			System.out.println("localVariableDeclaration without assignment");
		}
		state.activeLocalVariableDeclaration = null;
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
		int variablesCount = 1 /* this */+ methodDeclaration.getParameters().size() + numberLocalVariables /* boolean assignments */+ 1;
		Graph graph = new Graph(methodEntity, variablesCount);
		state.methodConstruction = new Construction(graph);

		Node args = graph.getArgs();
		// set this parameter
		createThisParameter(args);
		// create parameters variables
		for (ParameterDefinition param : methodDeclaration.getParameters()) {
			param.accept(this);
		}

		methodDeclaration.getBlock().accept(this);

		// add all collected return nodes
		for (Node returnNode : state.methodReturns.values()) {
			graph.getEndBlock().addPred(returnNode);
		}
		if (methodDeclaration.getType().getBasicType() == BasicType.VOID
				&& !state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
			// return void if no return was specified yet
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

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		Type type = parameterDefinition.getType();
		Node reference = state.methodConstruction.getGraph().getArgs();
		int variableNumber = state.methodVariableCount++;

		Node parameterProj = state.methodConstruction.newProj(reference,
				convertAstTypeToMode(type), variableNumber);

		state.methodConstruction.setVariable(variableNumber, parameterProj);
		// add parameter number to map
		state.methodVariables.put(new DefinitionKey(parameterDefinition.getIdentifier(), parameterDefinition.getType(),
				parameterDefinition), variableNumber);
		parameterDefinition.setFirmNode(parameterProj);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		assert "main".equals(staticMethodDeclaration.getIdentifier().getValue());

		clearState();

		int variablesCount = staticMethodDeclaration.getNumberOfLocalVariables()/* boolean assignments */+ 1;
		System.out.println("num local vars in main = " + variablesCount);
		Graph mainGraph = new Graph(state.hierarchy.getMainMethod(), variablesCount);
		this.state.methodConstruction = new Construction(mainGraph);

		staticMethodDeclaration.getBlock().accept(this);

		// add all collected return nodes
		for (Node returnNode : state.methodReturns.values()) {
			mainGraph.getEndBlock().addPred(returnNode);
		}
		if (!state.methodReturns.containsKey(state.methodConstruction.getCurrentBlock())) {
			// return void if no return was specified yet
			Node returnNode = state.methodConstruction.newReturn(state.methodConstruction.getCurrentMem(), new Node[] {});
			mainGraph.getEndBlock().addPred(returnNode);
		}

		state.methodConstruction.setUnreachable();
		state.methodConstruction.finish();
		// clearState map
		state.methodVariables.clear();
	}

	private void clearState() {
		this.state.assignmentRightSide = null;
		this.state.methodConstruction = null;
		this.state.methodVariables.clear();
		this.state.methodVariableCount = 0;
		this.state.methodReturns.clear();
		this.state.activePhiNode = null;
		this.state.activeLocalVariableDeclaration = null;
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

	@Override
	public void visit(Type type) {
		// Type is never been visited
	}

	@Override
	public void visit(ClassType classType) {
		// ClassType is never been visited
	}

	private class DefinitionKey {
		private final Symbol ident;
		private final Type type;
		private final AstNode node;

		private DefinitionKey(Symbol ident, Type type, AstNode node) {
			this.ident = ident;
			this.type = type;
			this.node = node;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ident == null) ? 0 : ident.hashCode());
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DefinitionKey other = (DefinitionKey) obj;
			if (ident == null) {
				if (other.ident != null)
					return false;
			} else if (!ident.equals(other.ident))
				return false;
			if (node == null) {
				if (other.node != null)
					return false;
			} else if (!node.equals(other.node))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

	}

}
