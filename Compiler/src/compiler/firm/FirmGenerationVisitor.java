package compiler.firm;

import java.util.List;

import compiler.Symbol;
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
import firm.CompoundType;
import firm.Construction;
import firm.Entity;
import firm.Graph;
import firm.MethodType;
import firm.Mode;
import firm.Mode.Arithmetic;
import firm.PrimitiveType;
import firm.Program;
import firm.nodes.Node;

public class FirmGenerationVisitor implements AstVisitor {

	private final Mode modeInt = Mode.getIs(); // integer signed 32 bit
	private final Mode modeBool = Mode.getBu(); // unsigned 8 bit
	private final Mode modeRef = Mode.createReferenceMode("P64", Arithmetic.TwosComplement, 64, 64); // 64 bit pointer

	/**
	 * Create and return a {@link firm.Type} for the given {@link Type}.
	 * 
	 * @param type
	 * @return
	 */
	private firm.Type createType(Type type) {
		Type tmpType = type;
		while (tmpType.getSubType() != null) {
			tmpType = tmpType.getSubType();
		}

		firm.Type firmType = null;
		switch (tmpType.getBasicType()) {
		case INT:
			firmType = new PrimitiveType(Mode.getIs());
			break;
		case BOOLEAN:
			firmType = new PrimitiveType(Mode.getBu());
			break;
		case VOID:
			// TODO: return null?
			break;
		case NULL:
			firmType = new PrimitiveType(modeRef);
			break;
		case CLASS:
		case METHOD:
			break;
		default:
			// no access allowed to public static void main(String[] args)
			// therefore no type needed for String
			// TODO:
			throw new RuntimeException();
		}

		if (type.getBasicType() == BasicType.ARRAY) {
			// create composite type
			firmType = new ArrayType(firmType);
		}

		return firmType;
	}

	/**
	 * Create and return a method type for the given {@link FieldDeclaration}.
	 * 
	 * @param decl
	 * @return
	 */
	private firm.Type createType(FieldDeclaration decl) {
		firm.Type type;

		if (decl.getType().getBasicType() == BasicType.CLASS) {
			type = new firm.ClassType(decl.getType().getIdentifier().getValue());
		} else {
			type = createType(decl.getType());
		}
		return type;
	}

	/**
	 * Create and return a method type for the given {@link ClassMember}.
	 * 
	 * @param decl
	 * @return
	 */
	private firm.Type createType(ClassMember decl) {
		if (decl instanceof FieldDeclaration) {
			return createType((FieldDeclaration) decl);
		} else {
			return createType((MethodDeclaration) decl);
		}
	}

	/**
	 * Create and return a method type for the given {@link MethodDeclaration}.
	 * 
	 * @param decl
	 * @return
	 */
	private MethodType createType(MethodDeclaration decl) {
		List<ParameterDefinition> params = decl.getParameters();
		firm.Type[] paramTypes = new firm.Type[params.size()];
		for (int i = 0; i < params.size(); i++) {
			paramTypes[i] = createType(params.get(i).getType());
		}

		firm.Type returnType = createType(decl.getType());

		// check if return type is void
		if (returnType == null) {
			return new MethodType(paramTypes, new firm.Type[] {});
		} else {
			return new MethodType(paramTypes, new firm.Type[] { returnType });
		}
	}

	/**
	 * Create and return a {@link firm.ClassType} for the given {@link ClassDeclaration}.
	 * 
	 * @param decl
	 * @return
	 */
	private firm.ClassType createClassType(ClassDeclaration decl) {
		firm.ClassType classType = new firm.ClassType(decl.getIdentifier().getValue());
		return classType;
	}

	/**
	 * Create and return an entity for the given {@link FieldDeclaration}, that is part of the given {@link ClassDeclaration} of type
	 * {@link CompoundType}.
	 * 
	 * @param decl
	 * @param classType
	 * @param member
	 * @param type
	 * @return
	 */
	private Entity createClassMemberEntity(ClassDeclaration decl, CompoundType classType, FieldDeclaration member, firm.Type type) {
		String name = decl.getIdentifier().getValue() + "#" + "f" + "#" + member.getIdentifier().getValue();
		Entity entity = new Entity(classType, name, type);
		entity.setLdIdent(name);
		return entity;
	}

	/**
	 * Create and return an entity for the given {@link MethodDeclaration}, that is part of the given {@link ClassDeclaration} of type
	 * {@link CompoundType}.
	 * 
	 * @param decl
	 * @param classType
	 * @param member
	 * @param type
	 * @return
	 */
	private Entity createClassMemberEntity(ClassDeclaration decl, CompoundType classType, MethodDeclaration member, firm.Type type) {
		String name = decl.getIdentifier().getValue() + "#" + "m" + "#" + member.getIdentifier().getValue();
		Entity entity = new Entity(classType, name, type);
		entity.setLdIdent(name);
		return entity;
	}

	private Entity createClassMemberEntity(ClassDeclaration decl, CompoundType classType, ClassMember member, firm.Type type) {
		if (member instanceof FieldDeclaration) {
			return createClassMemberEntity(decl, classType, (FieldDeclaration) member, type);
		} else {
			return createClassMemberEntity(decl, classType, (MethodDeclaration) member, type);
		}
	}

	private firm.Mode convertTypeToMode(Type type)
	{
		switch (type.getBasicType())
		{
		case INT:
			return modeInt;
		case BOOLEAN:
			return modeBool;
		case CLASS:
			return modeRef;
		default:
			throw new RuntimeException("convertTypeToMode for " + type + " is not implemented");
		}
	}

	// library function print_int in global scope
	private final Entity print_int;
	final Entity calloc;

	// current definitions
	private firm.ClassType currentClass = null;
	private Construction currentMethod = null;
	private String currentClassPrefix = "";
	private int currentMethodVariableCount = 0;

	public FirmGenerationVisitor() {
		// create library function(s)
		MethodType print_int_type = new MethodType(new firm.Type[] { new PrimitiveType(modeInt) }, new firm.Type[] {});
		this.print_int = new Entity(firm.Program.getGlobalType(), "#print_int", print_int_type);
		// void* calloc (size_t num, size_t size);
		MethodType calloc_type = new MethodType(new firm.Type[] { new PrimitiveType(modeInt), new PrimitiveType(modeInt) },
				new firm.Type[] { new PrimitiveType(modeRef) });
		this.calloc = new Entity(firm.Program.getGlobalType(), "#calloc", calloc_type);
	}

	@Override
	public void visit(AdditionExpression additionExpression) {

		// get type of expression
		Mode mode = convertTypeToMode(additionExpression.getType());

		// get firmNode for fist operand
		Expression operand1 = additionExpression.getOperand1();
		operand1.accept(this);
		Node operand1Node = operand1.getFirmNode();

		// get firmNode for second operand
		Expression operand2 = additionExpression.getOperand2();
		operand2.accept(this);
		Node operand2Node = operand2.getFirmNode();

		// TODO read operand type from expression type
		Node addExpr = currentMethod.newAdd(operand1Node, operand2Node, mode);
		additionExpression.setFirmNode(addExpr);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		System.out.println("assignmentExpression = [" + assignmentExpression + "]");
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

		// TODO check for errors in parseInt
		String intValue = integerConstantExpression.getIntegerLiteral();
		int val = Integer.parseInt(intValue);

		Node constant = currentMethod.newConst(val, modeInt);
		integerConstantExpression.setFirmNode(constant);
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
			// System.out.println("statement.getClass().getName() = " + statement.getClass().getName());
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
		int variableNumber = 0; // TODO get variable number
		Mode variableMode = convertTypeToMode(localVariableDeclaration.getType());

		Expression expression = localVariableDeclaration.getExpression();
		if (expression != null) {
			expression.accept(this);
		}

		Node firmNode = expression.getFirmNode();
		currentMethod.setVariable(variableNumber, firmNode);
		Node varNode = currentMethod.getVariable(variableNumber, variableMode);

		localVariableDeclaration.setFirmNode(varNode);
	}

	public void createParameterDefinition(Node args, ParameterDefinition parameterDefinition) {
		Node paramProj = currentMethod.newProj(args, convertTypeToMode(parameterDefinition.getType()), currentMethodVariableCount++);
		currentMethod.setVariable(currentMethodVariableCount, paramProj);

		parameterDefinition.setFirmNode(paramProj);
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		currentClass = createClassType(classDeclaration);
		for (ClassMember curr : classDeclaration.getMembers()) {
			currentClassPrefix = curr.getIdentifier().getValue();
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
		MethodType methodType = createType(methodDeclaration);
		Entity methodEntity = new Entity(Program.getGlobalType(), currentClassPrefix + "#f#" + methodDeclaration.getIdentifier().getValue(),
				methodType);

		currentMethodVariableCount = 0;
		int numberLocalVariables = methodDeclaration.getNumberOfLocalVariables();
		int variablesCount = 1 + methodDeclaration.getParameters().size() + numberLocalVariables;
		Graph graph = new Graph(methodEntity, variablesCount);
		currentMethod = new Construction(graph);

		// create parameters variables
		Node args = graph.getArgs();
		for (ParameterDefinition param : methodDeclaration.getParameters()) {
			createParameterDefinition(args, param);
		}

		Node returnNode = currentMethod.newReturn(currentMethod.getCurrentMem(), new Node[] { currentMethod.getCurrentMem() }); // TODO: in case of
																																// return, we have to
																																// add the node here

		if (methodDeclaration.getBlock().isEmpty()) {
			// return block has no predecessor!
		} else {
			methodDeclaration.getBlock().accept(this);
			returnNode.setPred(0, methodDeclaration.getBlock().getFirmNode());
		}

		graph.getEndBlock().addPred(returnNode);

		currentMethod.setUnreachable();
		currentMethod.finish();
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		Symbol methodName = staticMethodDeclaration.getIdentifier();
		// assume that methodName is main

		// we can't use argument of main and we have checked that it is not used
		// so mainType is void main(void)
		MethodType mainType = new MethodType(new firm.Type[] {}, new firm.Type[] {});
		Entity mainEntity = new Entity(Program.getGlobalType(), currentClassPrefix + "#f#" + methodName.getValue(), mainType);

		int variablesCount = 100; // TODO count variables
		Graph mainGraph = new Graph(mainEntity, variablesCount);
		this.currentMethod = new Construction(mainGraph);

		staticMethodDeclaration.getBlock().accept(this);

		// TODO: here it is necessary to check whether block contains return statements
		// TODO: and if it does, get it, otherwise return "void" as here
		// TODO: (if I understood correctly )if method returns void it is necessary to link last statement with return
		// TODO: otherwise it won't appear in graph
		Node returnNode = currentMethod.newReturn(currentMethod.getCurrentMem(), new Node[] {});
		returnNode.setPred(0, staticMethodDeclaration.getBlock().getFirmNode());
		mainGraph.getEndBlock().addPred(returnNode);

		currentMethod.setUnreachable();
		currentMethod.finish();
	}

	@Override
	public void visit(ClassType classType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		// TODO Auto-generated method stub

	}

}
