package compiler.parser.printer;

import java.util.Collections;
import java.util.List;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.MemberDeclaration;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDeclaration;
import compiler.ast.Program;
import compiler.ast.StaticMethodDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BlockBasedStatement;
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
import compiler.ast.type.ArrayType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.TokenType;

public class PrettyPrinterVisitor implements AstVisitor {
	private final StringBuilder stringBuilder = new StringBuilder(4096);

	private int precedence = 0;
	private int tabStops = 0;

	private void printTabs() {
		for (int i = 0; i < tabStops; i++) {
			stringBuilder.append('\t');
		}
	}

	/**
	 * Gets the result of the PrettyPrinterVisitor
	 * 
	 * @return
	 */
	public String getOutputString() {
		return stringBuilder.toString();
	}

	/**
	 * Visit a binary expression, use the TokenType to display the symbol and show the brackets.
	 * 
	 * @param binaryExpression
	 *            Expression to display
	 * @param tokenType
	 *            Type of the token.
	 */
	private void visit(BinaryExpression binaryExpression, TokenType tokenType) {
		int oldPrecedence = precedence;

		precedence = tokenType.getPrecedence();
		if (oldPrecedence > 0) {
			stringBuilder.append('(');
		}
		if (!tokenType.isLeftAssociative())
			precedence++;

		binaryExpression.getOperand1().accept(this);
		stringBuilder.append(' ');
		stringBuilder.append(tokenType.getString());
		stringBuilder.append(' ');

		precedence += tokenType.isLeftAssociative() ? 1 : 0;

		binaryExpression.getOperand2().accept(this);

		if (oldPrecedence > 0) {
			stringBuilder.append(')');
		}
		precedence = oldPrecedence;
	}

	@Override
	public void visit(AdditionExpression additionExpression) {
		visit(additionExpression, TokenType.ADD);
	}

	@Override
	public void visit(AssignmentExpression assignmentExpression) {
		visit(assignmentExpression, TokenType.ASSIGN);
	}

	@Override
	public void visit(DivisionExpression divisionExpression) {
		visit(divisionExpression, TokenType.DIVIDE);
	}

	@Override
	public void visit(EqualityExpression equalityExpression) {
		visit(equalityExpression, TokenType.EQUAL);
	}

	@Override
	public void visit(GreaterThanEqualExpression greaterThanEqualExpression) {
		visit(greaterThanEqualExpression, TokenType.GREATEREQUAL);
	}

	@Override
	public void visit(GreaterThanExpression greaterThanExpression) {
		visit(greaterThanExpression, TokenType.GREATER);
	}

	@Override
	public void visit(LessThanEqualExpression lessThanEqualExpression) {
		visit(lessThanEqualExpression, TokenType.LESSEQUAL);
	}

	@Override
	public void visit(LessThanExpression lessThanExpression) {
		visit(lessThanExpression, TokenType.LESS);
	}

	@Override
	public void visit(LogicalAndExpression logicalAndExpression) {
		visit(logicalAndExpression, TokenType.LOGICALAND);
	}

	@Override
	public void visit(LogicalOrExpression logicalOrExpression) {
		visit(logicalOrExpression, TokenType.LOGICALOR);
	}

	@Override
	public void visit(ModuloExpression moduloExpression) {
		visit(moduloExpression, TokenType.MODULO);
	}

	@Override
	public void visit(MuliplicationExpression multiplicationExpression) {
		visit(multiplicationExpression, TokenType.MULTIPLY);
	}

	@Override
	public void visit(NonEqualityExpression nonEqualityExpression) {
		visit(nonEqualityExpression, TokenType.NOTEQUAL);
	}

	@Override
	public void visit(SubtractionExpression substractionExpression) {
		visit(substractionExpression, TokenType.SUBTRACT);
	}

	@Override
	public void visit(BooleanConstantExpression booleanConstantExpression) {
		stringBuilder.append(booleanConstantExpression.isValue());
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		boolean withBrackets = precedence > 0 && integerConstantExpression.isNegative();

		if (withBrackets)
			stringBuilder.append('(');

		stringBuilder.append(integerConstantExpression.getIntegerLiteral());

		if (withBrackets)
			stringBuilder.append(')');
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		int oldPrecedence = precedence;
		precedence = 1;

		if (!methodInvocationExpression.isLocalMethod()) {
			if (oldPrecedence > 0)
				stringBuilder.append('(');

			methodInvocationExpression.getMethodExpression().accept(this);
			stringBuilder.append('.');
		}

		stringBuilder.append(methodInvocationExpression.getMethodIdentifier());
		stringBuilder.append('(');
		Expression[] args = methodInvocationExpression.getParameters();

		// print arguments
		if (args.length > 0) {
			int i = 0;
			precedence = 0;
			args[i++].accept(this);
			while (i < args.length) {
				stringBuilder.append(", ");
				precedence = 0;
				args[i++].accept(this);
			}

		}

		precedence = oldPrecedence;
		stringBuilder.append(')');

		if (!methodInvocationExpression.isLocalMethod() && oldPrecedence > 0)
			stringBuilder.append(')');
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		if (precedence > 0) {
			stringBuilder.append('(');
		}

		stringBuilder.append("new ");
		visitNewArrayExpression(newArrayExpression.getType(), newArrayExpression.getFirstDimension());

		if (precedence > 0) {
			stringBuilder.append(')');
		}
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		if (precedence > 0) {
			stringBuilder.append('(');
		}
		stringBuilder.append("new ");
		stringBuilder.append(newObjectExpression.getIdentifier());
		stringBuilder.append("()");

		if (precedence > 0) {
			stringBuilder.append(')');
		}
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		if (variableAccessExpression.getExpression() != null) {
			int oldPrecedence = precedence;
			precedence = 1;

			if (oldPrecedence > 0)
				stringBuilder.append('(');

			variableAccessExpression.getExpression().accept(this);
			stringBuilder.append('.');
			stringBuilder.append(variableAccessExpression.getFieldIdentifier().getValue());

			if (oldPrecedence > 0)
				stringBuilder.append(')');

			precedence = oldPrecedence;
		} else {
			stringBuilder.append(variableAccessExpression.getFieldIdentifier().getValue());
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		if (precedence > 0)
			stringBuilder.append('(');

		int oldPrecedence = precedence;
		precedence = 1;

		arrayAccessExpression.getArrayExpression().accept(this);
		stringBuilder.append('[');

		precedence = 0;

		arrayAccessExpression.getIndexExpression().accept(this);
		stringBuilder.append(']');

		precedence = oldPrecedence;

		if (precedence > 0)
			stringBuilder.append(')');
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		if (precedence > 0) {
			stringBuilder.append('(');
		}
		int oldPrecedence = precedence;
		precedence = 10;

		stringBuilder.append('!');
		logicalNotExpression.getOperand().accept(this);

		precedence = oldPrecedence;
		if (precedence > 0) {
			stringBuilder.append(')');
		}
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		if (precedence > 0) {
			stringBuilder.append('(');
		}
		int oldPrecedence = precedence;
		precedence = 10;

		stringBuilder.append('-');
		negateExpression.getOperand().accept(this);

		precedence = oldPrecedence;
		if (precedence > 0) {
			stringBuilder.append(')');
		}
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		if (returnStatement.getOperand() != null) {
			stringBuilder.append("return ");
			returnStatement.getOperand().accept(this);
		} else {
			stringBuilder.append("return");
		}
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		stringBuilder.append("this");
	}

	@Override
	public void visit(NullExpression nullExpression) {
		stringBuilder.append("null");
	}

	@Override
	public void visit(Type type) {
		visitType(type);
	}

	@Override
	public void visit(ClassType classType) {
		visitType(classType);
	}

	@Override
	public void visit(ArrayType arrayType) {
		visitType(arrayType);
	}

	public void visitType(Type type) {
		int dim = 0;
		while (type.getSubType() != null) {
			type = type.getSubType();
			dim++;
		}
		String typeString;
		switch (type.getBasicType()) {
		case INT:
			typeString = "int";
			break;
		case VOID:
			typeString = "void";
			break;
		case BOOLEAN:
			typeString = "boolean";
			break;
		case CLASS:
			typeString = type.getIdentifier().getValue();
			break;
		default:
			throw new IllegalArgumentException();
		}
		stringBuilder.append(typeString);
		for (int i = 0; i < dim; i++) {
			stringBuilder.append("[]");
		}
	}

	/**
	 * Print the type and the expression for new array[expression]([])*
	 * 
	 * @param type
	 * @param expr
	 */
	private void visitNewArrayExpression(Type type, Expression expr) {
		int dim = 0;
		while (type.getSubType() != null) {
			type = type.getSubType();
			dim++;
		}
		String typeString;
		switch (type.getBasicType()) {
		case INT:
			typeString = "int";
			break;
		case VOID:
			typeString = "void";
			break;
		case BOOLEAN:
			typeString = "boolean";
			break;
		case CLASS:
			typeString = type.getIdentifier().getValue();
			break;
		default:
			throw new IllegalArgumentException();
		}
		stringBuilder.append(typeString);

		stringBuilder.append('[');
		expr.accept(this);
		stringBuilder.append(']');

		for (int i = 1; i < dim; i++) {
			stringBuilder.append("[]");
		}
	}

	@Override
	public void visit(Block block) {
		List<Statement> statements = block.getStatements();

		if (statements.isEmpty()) {
			stringBuilder.append("{ }");
		} else {
			stringBuilder.append('{');
			tabStops++;

			for (Statement statement : statements) {
				stringBuilder.append('\n');
				printTabs();
				statement.accept(this);
				if (!(statement instanceof BlockBasedStatement) && !(statement instanceof Block)) {
					stringBuilder.append(';');
				}
			}

			tabStops--;
			stringBuilder.append('\n');
			printTabs();
			stringBuilder.append('}');
		}
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		stringBuilder.append("class ");
		stringBuilder.append(classDeclaration.getIdentifier());
		stringBuilder.append(" {");

		if (classDeclaration.getMembers().size() > 0) {
			stringBuilder.append('\n');
			tabStops++;

			List<MemberDeclaration> members = classDeclaration.getMembers();
			Collections.sort(members);

			for (MemberDeclaration member : members) {
				printTabs();
				member.accept(this);
			}
			tabStops--;
		} else {
			stringBuilder.append(' ');
		}
		stringBuilder.append("}\n");
	}

	@Override
	public void visit(IfStatement ifStatement) {
		stringBuilder.append("if (");
		ifStatement.getCondition().accept(this);
		stringBuilder.append(')');

		Statement trueCase = ifStatement.getTrueCase();
		boolean trueIsBlock = visitBlockOrStatement(trueCase);

		Statement falseCase = ifStatement.getFalseCase();
		if (falseCase != null) {
			if (trueIsBlock) {// if true case was a block, add space
				if (((Block) trueCase).getStatements().isEmpty()) { // if true block was empty
					stringBuilder.append('\n');
					printTabs();
				} else {
					stringBuilder.append(' ');
				}
			} else {
				stringBuilder.append('\n');
				printTabs();
			}

			// handle else if
			if (falseCase instanceof IfStatement) {
				stringBuilder.append("else ");
				falseCase.accept(this);

			} else { // handle normal else
				stringBuilder.append("else");
				visitBlockOrStatement(falseCase);
			}
		}
	}

	private boolean visitBlockOrStatement(Statement statement) {
		if (statement instanceof Block) {
			stringBuilder.append(' ');
			statement.accept(this);
			return true;
		} else {
			stringBuilder.append('\n');
			tabStops++;
			printTabs();

			statement.accept(this);
			if (!(statement instanceof BlockBasedStatement))
				stringBuilder.append(';');

			tabStops--;
			return false;
		}
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		stringBuilder.append("while (");
		whileStatement.getCondition().accept(this);
		stringBuilder.append(')');

		Statement body = whileStatement.getBody();
		visitBlockOrStatement(body);
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		localVariableDeclaration.getType().accept(this);
		stringBuilder.append(' ');
		stringBuilder.append(localVariableDeclaration.getIdentifier());
		Expression expression = localVariableDeclaration.getExpression();

		if (expression != null) {
			stringBuilder.append(" = ");
			expression.accept(this);
		}
	}

	@Override
	public void visit(ParameterDeclaration parameterDefinition) {
		parameterDefinition.getType().accept(this);
		stringBuilder.append(' ');
		stringBuilder.append(parameterDefinition.getIdentifier());
	}

	@Override
	public void visit(Program program) {
		List<ClassDeclaration> classes = program.getClasses();
		Collections.sort(classes);

		for (ClassDeclaration classDeclaration : classes) {
			classDeclaration.accept(this);
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		printMethodDeclaration(methodDeclaration, false);
	}

	@Override
	public void visit(StaticMethodDeclaration staticMethodDeclaration) {
		printMethodDeclaration(staticMethodDeclaration, true);
	}

	private void printMethodDeclaration(MethodDeclaration methodDeclaration, boolean isStatic) {
		stringBuilder.append("public ");
		if (isStatic)
			stringBuilder.append("static ");

		methodDeclaration.getType().accept(this);
		stringBuilder.append(' ');
		stringBuilder.append(methodDeclaration.getIdentifier());
		stringBuilder.append('(');
		boolean first = true;

		for (ParameterDeclaration parameter : methodDeclaration.getParameters()) {
			if (first)
				first = false;
			else
				stringBuilder.append(", ");
			parameter.accept(this);
		}

		stringBuilder.append(") ");
		Block block = methodDeclaration.getBlock();
		block.accept(this);
		stringBuilder.append('\n');
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		stringBuilder.append("public ");
		fieldDeclaration.getType().accept(this);
		stringBuilder.append(' ');
		stringBuilder.append(fieldDeclaration.getIdentifier().getValue());
		stringBuilder.append(";\n");
	}

}
