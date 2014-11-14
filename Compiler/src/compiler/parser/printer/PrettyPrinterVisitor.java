package compiler.parser.printer;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
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
import compiler.ast.statement.type.Type;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.visitor.AstVisitor;
import compiler.lexer.TokenType;

public class PrettyPrinterVisitor implements AstVisitor {
	private StringBuffer stringBuffer = new StringBuffer();

	// FIXME: { } should have no line breaks!

	private int tabStops = 0;

	private void printTabs() {
		for (int i = 0; i < tabStops; i++) {
			stringBuffer.append("\t");
		}
	}

	public void resetOutputStream() {
		this.stringBuffer = new StringBuffer();
	}

	/**
	 * Gets the result of the PrettyPrinterVisitor
	 * 
	 * @return
	 */
	public String getOutputString() {
		return this.stringBuffer.toString();
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
		// TODO Show only brackets if it is necessary.
		binaryExpression.getOperand1().accept(this);
		stringBuffer.append(tokenType.getString());
		binaryExpression.getOperand2().accept(this);
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
		stringBuffer.append(booleanConstantExpression.isValue());
	}

	@Override
	public void visit(IntegerConstantExpression integerConstantExpression) {
		stringBuffer.append(integerConstantExpression.getIntegerLiteral());
	}

	@Override
	public void visit(MethodInvocationExpression methodInvocationExpression) {
		// TODO: right format!
		// outputString += "(";

		// expr == null --> this.method()
		// otherwise expr.ident()
		if (!methodInvocationExpression.isLocalMethod()) {
			methodInvocationExpression.getMethodExpression().accept(this);
			stringBuffer.append(".");
		}
		// outputString += ")";
		stringBuffer.append(methodInvocationExpression.getMethodIdent() + "(");
		Expression[] args = methodInvocationExpression.getParameters();

		// print args
		if (args != null && args.length > 0) {
			int i = 0;
			args[i++].accept(this);
			while (i < args.length) {
				stringBuffer.append(", ");
				args[i++].accept(this);
			}
		}

		stringBuffer.append(")");
	}

	@Override
	public void visit(NewArrayExpression newArrayExpression) {
		// TODO: right format!
		stringBuffer.append("(new ");
		newArrayExpression.getType().accept(this);
		stringBuffer.append("[");
		newArrayExpression.getFirstDimension().accept(this);
		stringBuffer.append("]");
		int dim = newArrayExpression.getDimensions();

		// print ([])*
		for (int i = 1; i < dim; i++) {
			stringBuffer.append("[]");
		}
		stringBuffer.append(")");
	}

	@Override
	public void visit(NewObjectExpression newObjectExpression) {
		// TODO: right format!
		stringBuffer.append("new " + newObjectExpression.getIdentifier() + "()");
	}

	@Override
	public void visit(VariableAccessExpression variableAccessExpression) {
		// outputString += variableAccessExpression.getIdentifier().getValue();
		// TODO: right format!
		if (variableAccessExpression.getExpression() != null) {
			variableAccessExpression.getExpression().accept(this);
			stringBuffer.append(".");
			stringBuffer.append(variableAccessExpression.getFieldIdentifier().getValue());
		} else {
			stringBuffer.append(variableAccessExpression.getFieldIdentifier().getValue());
		}
	}

	@Override
	public void visit(ArrayAccessExpression arrayAccessExpression) {
		// TODO: right format!
		// stringBuffer.append("(");
		arrayAccessExpression.getArrayExpression().accept(this);
		stringBuffer.append("[");
		arrayAccessExpression.getIndexExpression().accept(this);
		stringBuffer.append("]");
	}

	@Override
	public void visit(LogicalNotExpression logicalNotExpression) {
		stringBuffer.append("!");
		logicalNotExpression.getOperand().accept(this);
	}

	@Override
	public void visit(NegateExpression negateExpression) {
		stringBuffer.append("-");
		negateExpression.getOperand().accept(this);
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		stringBuffer.append("return ");
		returnStatement.getOperand().accept(this);
	}

	@Override
	public void visit(ThisExpression thisExpression) {
		stringBuffer.append("this");
	}

	@Override
	public void visit(NullExpression nullExpression) {
		stringBuffer.append("null");
	}

	@Override
	public void visit(Type type) {
		while (type.getSubType() != null) {
			type = type.getSubType();
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
		stringBuffer.append(typeString);
	}

	@Override
	public void visit(Block block) {
		stringBuffer.append("{");
		if (block.getStatements() != null && block.getStatements().size() > 0) {
			tabStops++;
			stringBuffer.append("\n");

			for (Statement statement : block.getStatements()) {
				printTabs();
				statement.accept(this);
				stringBuffer.append(";\n");
			}
			tabStops--;
		} else {
			stringBuffer.append(" ");
		}
		stringBuffer.append("}\n");
	}

	@Override
	public void visit(ClassDeclaration classDeclaration) {
		stringBuffer.append("class " + classDeclaration.getIdentifier() + " {");

		if (classDeclaration.getMembers() != null && classDeclaration.getMembers().size() > 0) {
			stringBuffer.append("\n");
			tabStops++;

			// TODO: Sort ClassMembers
			for (ClassMember member : classDeclaration.getMembers()) {
				printTabs();
				member.accept(this);
				stringBuffer.append("\n");
			}
			tabStops--;
		} else {
			stringBuffer.append(" ");
		}
		stringBuffer.append("}\n");
	}

	@Override
	public void visit(IfStatement ifStatement) {
		printTabs();
		stringBuffer.append("if (");
		ifStatement.getCondition().accept(this);
		stringBuffer.append(") ");
		ifStatement.getTrueCase().accept(this);

		Statement falseCase = ifStatement.getFalseCase();
		if (falseCase != null) { // TODO: Is that correct this way?
			printTabs();
			stringBuffer.append(" else ");
			falseCase.accept(this);
		}
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		printTabs();
		stringBuffer.append("while (");
		whileStatement.getCondition().accept(this);
		stringBuffer.append(") ");
		whileStatement.getBody().accept(this);
	}

	@Override
	public void visit(LocalVariableDeclaration localVariableDeclaration) {
		localVariableDeclaration.getType().accept(this);
		stringBuffer.append(" " + localVariableDeclaration.getIdentifier());
		Expression expression = localVariableDeclaration.getExpression();

		if (expression != null) {
			stringBuffer.append(" = ");
			expression.accept(this);
		}
	}

	@Override
	public void visit(ParameterDefinition parameterDefinition) {
		parameterDefinition.getType().accept(this);
		stringBuffer.append(" " + parameterDefinition.getIdentifier());
	}

	@Override
	public void visit(Program program) {
		for (ClassDeclaration classDeclaration : program.getClasses()) {
			classDeclaration.accept(this);
		}
	}

	@Override
	public void visit(MethodDeclaration methodDeclaration) {
		stringBuffer.append("public ");
		methodDeclaration.getType().accept(this);
		stringBuffer.append(" " + methodDeclaration.getIdentifier() + "() ");
		methodDeclaration.getBlock().accept(this);
	}

	@Override
	public void visit(FieldDeclaration fieldDeclaration) {
		stringBuffer.append("public ");
		fieldDeclaration.getType().accept(this);
		stringBuffer.append(" " + fieldDeclaration.getIdentifier().getValue());

	}

}
