package compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import compiler.ast.statement.Expression;
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
import compiler.ast.statement.type.ArrayType;
import compiler.ast.statement.type.BasicType;
import compiler.ast.statement.type.ClassType;
import compiler.ast.statement.type.Type;
import compiler.ast.statement.unary.LogicalNotExpression;
import compiler.ast.statement.unary.NegateExpression;
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.lexer.OperationType;
import compiler.lexer.Position;
import compiler.lexer.Token;
import compiler.lexer.TokenSuppliable;
import compiler.lexer.TokenType;

public class Parser {

	private final TokenSuppliable tokenSupplier;
	/**
	 * Current token.
	 */
	private Token token;
	private int errorsDetected;
	private Program ast;

	public Parser(TokenSuppliable tokenSupplier) throws IOException {
		this.tokenSupplier = tokenSupplier;
		token = tokenSupplier.getNextToken();
		errorsDetected = 0;
	}

	/**
	 * Parse.
	 * 
	 * @return the number of errors detected.
	 * @throws IOException
	 * @throws ParsingFailedException
	 */
	public Program parse() throws IOException, ParsingFailedException {
		try {
			ast = parseProgram();
		} catch (ParserException e) {
			errorsDetected++;
			System.err.println(e);
		}

		if (errorsDetected == 0) {
			return ast;
		} else {
			throw new ParsingFailedException(errorsDetected);
		}
	}

	/**
	 * Program -> epsilon | class IDENT { ClassMember' } Program
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Program parseProgram() throws IOException, ParserException {
		Program program = new Program(token.getPosition());

		// ClassDeclaration*
		while (token.getType() != TokenType.EOF) {
			try {
				expectAndConsume(TokenType.CLASS);
				// save token attributes
				Position pos = token.getPosition();
				Symbol symbol = token.getSymbol();
				expectAndConsume(TokenType.IDENTIFIER);

				ClassDeclaration classDecl = new ClassDeclaration(pos, symbol);

				expectAndConsume(TokenType.LCURLYBRACKET);

				if (isTokenTypeAndConsume(TokenType.RCURLYBRACKET)) {
					program.addClassDeclaration(classDecl);
					continue;
				} else {
					// there is at least one ClassMember
					while (token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
						try {
							classDecl.addClassMember(parseClassMember());
						} catch (ParserException e) {
							errorsDetected++;
							System.err.println(e);
							while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.RCURLYBRACKET
									&& token.getType() != TokenType.EOF) {
								token = tokenSupplier.getNextToken();
							}
							// never consume EOF
							if (token.getType() != TokenType.EOF)
								token = tokenSupplier.getNextToken();
						}
					}
					// throw another error in case our previous error handling consumed the last }
					notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
					program.addClassDeclaration(classDecl);
				}
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
				// never consume EOF
				if (token.getType() != TokenType.EOF)
					token = tokenSupplier.getNextToken();
			}

		}
		// No ClassDeclaration => Epsilon
		if (token.getType() != TokenType.EOF) {
			throw new ParserException(token, TokenType.EOF);
		}

		return program;
	}

	/**
	 * ClassMember' -> public MainMethod' | public Type IDENT ; | public Type IDENT ( Parameters? ) Block MainMethod' -> static void IDENT ( String [
	 * ] IDENT ) Block
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ClassMember parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			token = tokenSupplier.getNextToken();
			// Type
			if (token.getType() == TokenType.INT
					|| token.getType() == TokenType.BOOLEAN
					|| token.getType() == TokenType.VOID
					|| token.getType() == TokenType.IDENTIFIER) {
				Type type = parseType();

				Token firstToken = token;
				if (isTokenTypeAndConsume(TokenType.IDENTIFIER)) {
					// public Type IDENT ;
					if (isTokenTypeAndConsume(TokenType.SEMICOLON)) {
						// accept
						return new FieldDeclaration(firstToken.getPosition(), type, firstToken.getSymbol());
						// public Type IDENT ( Parameters? ) Block
					} else if (isTokenTypeAndConsume(TokenType.LP)) {
						MethodDeclaration methDecl = new MethodDeclaration(firstToken.getPosition(), firstToken.getSymbol(), type);

						if (isTokenTypeAndConsume(TokenType.RP)) {
						} else {
							parseParameters(methDecl);
							expectAndConsume(TokenType.RP);
						}
						methDecl.setBlock(parseBlock());
						return methDecl;
					} else {
						throw new ParserException(token);
					}
				} else {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				// MainMethod' -> static void IDENT ( String [ ] IDENT ) Block
			} else if (isTokenTypeAndConsume(TokenType.STATIC)) {
				// save void return type
				Token retType = token;
				expectAndConsume(TokenType.VOID);

				// save identifier
				Token firstToken = token;
				expectAndConsume(TokenType.IDENTIFIER);
				expectAndConsume(TokenType.LP);

				Token stringToken = token;
				expectAndConsume(TokenType.IDENTIFIER);
				// identifier must be "String"
				if (!stringToken.getSymbol().getValue().equals("String")) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				Position pos = stringToken.getPosition();
				Symbol type = stringToken.getSymbol();

				expectAndConsume(TokenType.LSQUAREBRACKET);
				expectAndConsume(TokenType.RSQUAREBRACKET);

				// save identifier symbol
				Symbol ident = token.getSymbol();
				expectAndConsume(TokenType.IDENTIFIER);
				expectAndConsume(TokenType.RP);

				// new main method ast node
				ParameterDefinition param = new ParameterDefinition(pos, new ArrayType(pos, new ClassType(pos, type)), ident);
				MethodDeclaration decl = new StaticMethodDeclaration(firstToken.getPosition(), firstToken.getSymbol(),
						new Type(retType.getPosition(), BasicType.VOID), parseBlock());
				decl.addParameter(param);
				return decl;
			}
		default:
			throw new ParserException(token, TokenType.PUBLIC);
		}
	}

	/**
	 * Parameters -> Parameter (, Parameters)?
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseParameters(MethodDeclaration methDecl) throws IOException, ParserException {
		methDecl.addParameter(parseParameter());
		if (isTokenTypeAndConsume(TokenType.COMMA)) {
			parseParameters(methDecl);
		}
	}

	/**
	 * Parameter -> Type IDENT
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ParameterDefinition parseParameter() throws ParserException, IOException {
		Type type = parseType();
		Token identToken = token;
		expectAndConsume(TokenType.IDENTIFIER);
		ParameterDefinition param = new ParameterDefinition(identToken.getPosition(), type, identToken.getSymbol());
		return param;
	}

	/**
	 * Type -> Basictype ([])
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Type parseType() throws IOException, ParserException {
		Type type = null;

		switch (token.getType()) {
		case INT:
			type = new Type(token.getPosition(), BasicType.INT);
			token = tokenSupplier.getNextToken();
			break;
		case BOOLEAN:
			type = new Type(token.getPosition(), BasicType.BOOLEAN);
			token = tokenSupplier.getNextToken();
			break;
		case VOID:
			type = new Type(token.getPosition(), BasicType.VOID);
			token = tokenSupplier.getNextToken();
			break;
		case IDENTIFIER:
			type = new ClassType(token.getPosition(), token.getSymbol());
			token = tokenSupplier.getNextToken();
			break;
		default:
			throw new ParserException(token);
		}

		while (isTokenTypeAndConsume(TokenType.LSQUAREBRACKET)) {
			Position pos = token.getPosition();
			expectAndConsume(TokenType.RSQUAREBRACKET);

			type = new ArrayType(pos, type); // TODO: seems to be ugly
		}

		return type;
	}

	/**
	 * Statement -> Block | EmptyStatement | IfStatement | ExpressionStatement | WhileStatement | ReturnStatement
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Block parseStatement() throws IOException, ParserException {
		switch (token.getType()) {
		// block
		case LCURLYBRACKET:
			return parseBlock();
			// empty statement
		case SEMICOLON:
			parseEmptyStatement();
			return new Block(token.getPosition());
			// if statement
		case IF:
			return new Block(parseIfStatement());
			// while statement
		case WHILE:
			return new Block(parseWhileStatement());
			// return statement
		case RETURN:
			return new Block(parseReturnStatement());
			// expression: propagate error recognition to parseExpressionStatement
		default:
			return new Block(parseExpressionStatement());
		}
	}

	/**
	 * Block -> { BlockStatement* }
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private Block parseBlock() throws ParserException, IOException {
		Position pos = token.getPosition();
		expectAndConsume(TokenType.LCURLYBRACKET);
		Block block = new Block(pos);

		while (token.getType() != TokenType.RCURLYBRACKET) {
			parseBlockStatement(block);
		}
		expectAndConsume(TokenType.RCURLYBRACKET);
		return block;
	}

	/**
	 * BlockStatement -> Block | EmptyStatement | IfStatement | ExpressionStatement | WhileStatement | ReturnStatement |
	 * LocalVariableDeclarationStatement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseBlockStatement(Block block) throws ParserException, IOException {
		switch (token.getType()) {
		// block
		case LCURLYBRACKET:
			try {
				block.addStatement(parseBlock());
				break;
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
				// throw another error in case our previous error handling consumed the last }
				notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// empty statement
		case SEMICOLON:
			parseEmptyStatement();
			break;
		// if statement
		case IF:
			try {
				block.addStatement(parseIfStatement());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
				// throw another error in case our previous error handling consumed the last }
				notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// while statement
		case WHILE:
			try {
				block.addStatement(parseWhileStatement());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
				// throw another error in case our previous error handling consumed the last }
				notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// return statement
		case RETURN:
			try {
				block.addStatement(parseReturnStatement());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
				// throw another error in case our previous error handling consumed the last }
				notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// ExpressionStatement or LocalVariableDeclarationStatement
		case INT:
		case BOOLEAN:
		case VOID:
			block.addStatement(parseLocalVariableDeclarationStatement());
			break;
		case IDENTIFIER:
			// get 2 tokens look ahead
			Token lookAhead = tokenSupplier.getLookAhead();
			if (lookAhead.getType() == TokenType.IDENTIFIER) {
				try {
					block.addStatement(parseLocalVariableDeclarationStatement());
				} catch (ParserException e) {
					errorsDetected++;
					System.err.println(e);
					while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.EOF) {
						token = tokenSupplier.getNextToken();
					}
					// throw another error in case our previous error handling consumed the last }
					notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
				}
				break;
			} else if (lookAhead.getType() == TokenType.LSQUAREBRACKET) {
				if (tokenSupplier.get2LookAhead().getType() == TokenType.RSQUAREBRACKET) {
					try {
						block.addStatement(parseLocalVariableDeclarationStatement());
					} catch (ParserException e) {
						errorsDetected++;
						System.err.println(e);
						while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.EOF) {
							token = tokenSupplier.getNextToken();
						}
						// throw another error in case our previous error handling consumed the last }
						notExpectAndConsume(TokenType.EOF, TokenType.RCURLYBRACKET);
					}
					break;
				}
				// expression: fall through to outer default case
			}
		default:
			try {
				block.addStatement(parseExpression());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				while (token.getType() != TokenType.SEMICOLON && token.getType() != TokenType.EOF) {
					token = tokenSupplier.getNextToken();
				}
			}

			if (token.getType() != TokenType.SEMICOLON) {
				throw new ParserException(token, TokenType.SEMICOLON);
			}

			token = tokenSupplier.getNextToken();
		}
	}

	/**
	 * LocalVariableDeclarationStatement -> Type IDENT (= Expression)? ;
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private LocalVariableDeclaration parseLocalVariableDeclarationStatement() throws IOException,
			ParserException {
		Type type = parseType();
		Token firstToken = token;
		expectAndConsume(TokenType.IDENTIFIER);

		Expression expr = null;
		if (isTokenTypeAndConsume(TokenType.ASSIGN)) {
			expr = parseExpression();
		}
		expectAndConsume(TokenType.SEMICOLON);
		return expr == null ? new LocalVariableDeclaration(firstToken.getPosition(), type, firstToken.getSymbol()) :
				new LocalVariableDeclaration(firstToken.getPosition(), type, firstToken.getSymbol(), expr);
	}

	/**
	 * EmptyStatement -> ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseEmptyStatement() throws ParserException, IOException {
		expectAndConsume(TokenType.SEMICOLON);
	}

	/**
	 * WhileStatement-> while ( Expression ) Statement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private WhileStatement parseWhileStatement() throws ParserException, IOException {
		// save first token
		Token firstToken = token;
		expectAndConsume(TokenType.WHILE);
		expectAndConsume(TokenType.LP);
		Expression expr = parseExpression();
		expectAndConsume(TokenType.RP);

		Block statement = parseStatement();
		return new WhileStatement(firstToken.getPosition(), expr, statement);
	}

	/**
	 * IfStatement -> if ( Expression ) Statement (else Statement)?
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private IfStatement parseIfStatement() throws ParserException, IOException {
		Token firstToken = token;
		expectAndConsume(TokenType.IF);
		expectAndConsume(TokenType.LP);
		Expression expr = parseExpression();
		expectAndConsume(TokenType.RP);
		Block trueStmt = parseStatement();

		if (isTokenTypeAndConsume(TokenType.ELSE)) {
			Block falseStmt = parseStatement();
			return new IfStatement(firstToken.getPosition(), expr, trueStmt, falseStmt);
		} else {
			return new IfStatement(firstToken.getPosition(), expr, trueStmt);
		}
	}

	/**
	 * ExpressionStatement -> Expression ;
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseExpressionStatement() throws IOException, ParserException {
		Expression expr = parseExpression();
		expectAndConsume(TokenType.SEMICOLON);
		return expr;
	}

	/**
	 * ReturnStatement -> return Expression? ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ReturnStatement parseReturnStatement() throws ParserException, IOException {
		Token firstToken = token;
		expectAndConsume(TokenType.RETURN);

		Expression expr = null;
		if (token.getType() != TokenType.SEMICOLON) {
			expr = parseExpression();
		}

		expectAndConsume(TokenType.SEMICOLON);
		return new ReturnStatement(firstToken.getPosition(), expr);
	}

	/**
	 * Parse an expression with precedence climbing. Initialize with default precedence 0. This method look up in the token type, if the token is an
	 * binary operation and parse left and right an unaryExpression.
	 * 
	 * @author Valentin Zickner
	 * @return Parsed Expression
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseExpression() throws IOException, ParserException {
		return parseExpression(0);
	}

	/**
	 * Work method for precedence climbing, use the parseExpression() method.
	 * 
	 * @author Valentin Zickner
	 * @param minPrecedence
	 *            Minimal precedence to parse.
	 * @return Parsed Expression
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseExpression(int minPrecedence) throws IOException, ParserException {
		Expression result = parseUnaryExpression();

		while (token != null && token.getType().getOperationType() == OperationType.BINARY &&
				token.getType().getPrecedence() >= minPrecedence) {
			Token operationToken = token;
			TokenType operationTokenType = operationToken.getType();
			token = tokenSupplier.getNextToken();

			int precedence = operationTokenType.getPrecedence();
			if (operationTokenType.isLeftAssociative()) {
				precedence++;
			}

			Expression rhs = parseExpression(precedence);

			Position position = token != null ? token.getPosition() : null;

			switch (operationTokenType) {
			case NOTEQUAL:
				result = new NonEqualityExpression(position, result, rhs);
				break;
			case MULTIPLY:
				result = new MuliplicationExpression(position, result, rhs);
				break;
			case ADD:
				result = new AdditionExpression(position, result, rhs);
				break;
			case SUBTRACT:
				result = new SubtractionExpression(position, result, rhs);
				break;
			case DIVIDE:
				result = new DivisionExpression(position, result, rhs);
				break;
			case LESSEQUAL:
				result = new LessThanEqualExpression(position, result, rhs);
				break;
			case LESS:
				result = new LessThanExpression(position, result, rhs);
				break;
			case EQUAL:
				result = new EqualityExpression(position, result, rhs);
				break;
			case ASSIGN:
				result = new AssignmentExpression(position, result, rhs);
				break;
			case GREATEREQUAL:
				result = new GreaterThanEqualExpression(position, result, rhs);
				break;
			case GREATER:
				result = new GreaterThanExpression(position, result, rhs);
				break;
			case MODULO:
				result = new ModuloExpression(position, result, rhs);
				break;
			case LOGICALAND:
				result = new LogicalAndExpression(position, result, rhs);
				break;
			case LOGICALOR:
				result = new LogicalOrExpression(position, result, rhs);
				break;
			default:
				throw new ParserException(operationToken);
			}
		}
		return result;
	}

	/**
	 * UnaryExpression -> PostfixExpression | (! | -) UnaryExpression
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseUnaryExpression() throws IOException, ParserException {
		Position pos;
		switch (token.getType()) {
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case LP:
		case NEW:
			return parsePostfixExpression();
		case LOGICALNOT:
			pos = token.getPosition();
			token = tokenSupplier.getNextToken();
			return new LogicalNotExpression(pos, parseUnaryExpression());
		case SUBTRACT:
			pos = token.getPosition();
			token = tokenSupplier.getNextToken();
			return new NegateExpression(pos, parseUnaryExpression());
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * PostfixExpression -> PrimaryExpression (PostfixOp)*
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parsePostfixExpression() throws IOException, ParserException {
		switch (token.getType()) {
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case LP:
		case NEW:
			Expression expr = parsePrimaryExpression();
			while (token.getType() == TokenType.LSQUAREBRACKET
					|| token.getType() == TokenType.POINT) {
				expr = parsePostfixOp(expr);
			}
			return expr;
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * PostfixOp -> MethodInvocationFieldAccess | ArrayAccess
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private PostfixExpression parsePostfixOp(Expression leftExpression) throws IOException, ParserException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			return new ArrayAccessExpression(leftExpression.getPosition(), leftExpression, parseArrayAccess());
		case POINT:
			return parsePostfixOpMethodField(leftExpression);
		default:
			throw new ParserException(token);
		}

	}

	/**
	 * MethodInvocationFieldAccess -> . IDENT MethodInvocationFieldAccess'
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private PostfixExpression parsePostfixOpMethodField(Expression leftExpression) throws IOException,
			ParserException {
		if (isTokenTypeAndConsume(TokenType.POINT)) {
			Symbol symbol = token.getSymbol();
			if (isTokenTypeAndConsume(TokenType.IDENTIFIER)) {
				return parseMethodInvocationFieldAccess(leftExpression, symbol);
			} else {
				throw new ParserException(token, TokenType.IDENTIFIER);
			}
		} else {
			throw new ParserException(token, TokenType.POINT);
		}
	}

	/**
	 * MethodInvocationFieldAccess' -> ( Arguments ) | epsilon
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private PostfixExpression parseMethodInvocationFieldAccess(Expression leftExpression, Symbol ident) throws IOException, ParserException {
		switch (token.getType()) {
		case LP:
			// method invocation
			token = tokenSupplier.getNextToken();
			Expression[] args = parseArguments();
			expectAndConsume(TokenType.RP);
			return new MethodInvocationExpression(leftExpression.getPosition(), leftExpression, ident, args);
		default:
			return new VariableAccessExpression(leftExpression.getPosition(), leftExpression, ident);
		}
	}

	/**
	 * ArrayAccess -> [ Expression ]
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseArrayAccess() throws IOException, ParserException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			token = tokenSupplier.getNextToken();
			Expression expr = parseExpression();
			if (isTokenTypeAndConsume(TokenType.RSQUAREBRACKET)) {
				return expr;
			} else {
				throw new ParserException(token, TokenType.RSQUAREBRACKET);
			}
		default:
			throw new ParserException(token, TokenType.LSQUAREBRACKET);
		}
	}

	/**
	 * Arguments -> (Expression (, Expression)*)?
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression[] parseArguments() throws IOException, ParserException {
		switch (token.getType()) {
		case LP:
		case LOGICALNOT:
		case SUBTRACT:
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case NEW:
			List<Expression> args = new ArrayList<Expression>();
			args.add(parseExpression());
			while (isTokenTypeAndConsume(TokenType.COMMA)) {
				args.add(parseExpression());
			}
			Expression[] argsArray = new Expression[args.size()];
			return args.toArray(argsArray); // list of arguments
			// no expression
		case RP:
		default:
			return new Expression[0]; // no ast
		}
	}

	/**
	 * PrimaryExpression -> null | false | true | INTEGER_LITERAL | PrimaryIdent | this | ( Expression ) | new NewExpression
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parsePrimaryExpression() throws IOException, ParserException {
		Position pos = token.getPosition();
		Symbol symbol = token.getSymbol();
		switch (token.getType()) {
		case FALSE:
			token = tokenSupplier.getNextToken();
			return new BooleanConstantExpression(pos, false);
		case TRUE:
			token = tokenSupplier.getNextToken();
			return new BooleanConstantExpression(pos, true);
		case INTEGER:
			token = tokenSupplier.getNextToken();
			return new IntegerConstantExpression(pos, symbol.getValue());
		case NULL:
			token = tokenSupplier.getNextToken();
			return new NullExpression(pos);
		case THIS:
			token = tokenSupplier.getNextToken();
			return new ThisExpression(pos);
		case IDENTIFIER:
			return parsePrimaryExpressionIdent();
		case LP:
			token = tokenSupplier.getNextToken();
			Expression expr = parseExpression();
			expectAndConsume(TokenType.RP);
			return expr;
		case NEW:
			token = tokenSupplier.getNextToken();
			return parseNewExpression(pos);
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * PrimaryIdent -> IDENT | IDENT ( Arguments )
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private PostfixExpression parsePrimaryExpressionIdent() throws IOException,
			ParserException {
		switch (token.getType()) {
		case IDENTIFIER:
			Position pos = token.getPosition();
			Symbol symbol = token.getSymbol();
			token = tokenSupplier.getNextToken();
			if (isTokenTypeAndConsume(TokenType.LP)) {
				Expression[] args = parseArguments();
				if (isTokenTypeAndConsume(TokenType.RP)) {
					// no access object
					return new MethodInvocationExpression(pos, null, symbol, args);
				} else {
					throw new ParserException(token, TokenType.RP);
				}
			}
			// assume "PrimaryIdent -> IDENT" when another token than '(' is
			// read
			return new VariableAccessExpression(pos, null, symbol);
		default:
			throw new ParserException(token, TokenType.IDENTIFIER);
		}
	}

	/**
	 * NewExpression -> IDENT () | BasicType NewArrayExpression
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private PrimaryExpression parseNewExpression(Position pos) throws IOException, ParserException {
		Type type;
		switch (token.getType()) {
		case IDENTIFIER:
			// new object or new array
			Symbol symbol = token.getSymbol();
			token = tokenSupplier.getNextToken();
			if (isTokenTypeAndConsume(TokenType.LP)) {
				// new object
				if (isTokenTypeAndConsume(TokenType.RP)) {
					return new NewObjectExpression(pos, symbol);
				} else {
					throw new ParserException(token, TokenType.RP);
				}
			} else if (token.getType() == TokenType.LSQUAREBRACKET) {
				// new array
				type = new ClassType(pos, symbol);
				return parseNewArrayExpressionHelp(pos, type);
			} else {
				throw new ParserException(token);
			}
		case INT:
			type = new Type(pos, BasicType.INT);
			break;
		case BOOLEAN:
			type = new Type(pos, BasicType.BOOLEAN);
			break;
		case VOID:
			type = new Type(pos, BasicType.VOID);
			break;
		default:
			throw new ParserException(token);
		}
		// new array
		token = tokenSupplier.getNextToken();
		if (token.getType() == TokenType.LSQUAREBRACKET) {
			// new array
			return parseNewArrayExpressionHelp(pos, type);
		} else {
			throw new ParserException(token, TokenType.LSQUAREBRACKET);
		}
	}

	/**
	 * NewArrayExpression -> [Expression] ([])*
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private NewArrayExpression parseNewArrayExpressionHelp(Position pos, Type type) throws IOException,
			ParserException {
		if (isTokenTypeAndConsume(TokenType.LSQUAREBRACKET)) {
			Expression expression = parseExpression();
			if (isTokenTypeAndConsume(TokenType.RSQUAREBRACKET)) {
				ArrayType arrayType = new ArrayType(pos, type);
				while (token.getType() == TokenType.LSQUAREBRACKET) {
					Token lookahead = tokenSupplier.getLookAhead();
					if (lookahead.getType() == TokenType.RSQUAREBRACKET) {
						// read both
						token = tokenSupplier.getNextToken();
						token = tokenSupplier.getNextToken();
						arrayType = new ArrayType(pos, arrayType);
					} else {
						// read [, but not part of NewArrayExpression
						// could be [Expression]ArrayAccess
						// [Expression][Expression]
						break;
					}
				}
				return new NewArrayExpression(pos, arrayType, expression);
			} else {
				throw new ParserException(token, TokenType.RSQUAREBRACKET);
			}
		} else {
			throw new ParserException(token, TokenType.LSQUAREBRACKET);
		}
	}

	/**
	 * Checks if the next token has the given token type and then consumes the token.
	 * 
	 * @param tokenType
	 * @throws ParserException
	 *             if the token type does not match
	 * @throws IOException
	 */
	private void expectAndConsume(TokenType tokenType) throws ParserException, IOException {
		expect(tokenType);
		consume();
	}

	/**
	 * Checks if the next token has the given token type and throws an exception.
	 * 
	 * @param tokenType
	 * @throws ParserException
	 *             if the token type does match
	 * @throws IOException
	 */
	private void notExpectAndConsume(TokenType tokenType, TokenType expected) throws ParserException, IOException {
		if (token.getType() == tokenType) {
			throw new ParserException(token, expected);
		}
		consume();
	}

	/**
	 * Checks if the next token has the given token type and then consumes the token.
	 * 
	 * @param tokenType
	 * @return true if the token type matched, false otherwise
	 * @throws IOException
	 */
	private boolean isTokenTypeAndConsume(TokenType tokenType) throws IOException {
		if (token.getType() == tokenType) {
			consume();
			return true;
		}
		return false;
	}

	/**
	 * Consume the next token.
	 * 
	 * @throws IOException
	 */
	private void consume() throws IOException {
		token = tokenSupplier.getNextToken();
	}

	/**
	 * Checks if the next token has the given token type.
	 * 
	 * @param tokenType
	 * @throws ParserException
	 *             if the token type does not match
	 */
	private void expect(TokenType tokenType) throws ParserException {
		if (token.getType() != tokenType) {
			throw new ParserException(token, tokenType);
		}
	}
}
