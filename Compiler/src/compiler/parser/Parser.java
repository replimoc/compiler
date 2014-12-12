package compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.MemberDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.declaration.StaticMethodDeclaration;
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
import compiler.ast.statement.unary.PostfixExpression;
import compiler.ast.statement.unary.PrimaryExpression;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.ast.type.ArrayType;
import compiler.ast.type.BasicType;
import compiler.ast.type.ClassType;
import compiler.ast.type.Type;
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
		consumeToken();
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
		while (isNotTokenType(TokenType.EOF)) {
			try {
				// class IDENT {
				expectAndConsume(TokenType.CLASS);
				expect(TokenType.IDENTIFIER);
				ClassDeclaration classDecl = new ClassDeclaration(token.getPosition(), token.getSymbol());
				consumeToken();
				expectAndConsume(TokenType.LCURLYBRACKET);

				if (isTokenType(TokenType.RCURLYBRACKET)) {
					consumeToken();
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
							consumeUntil(TokenType.SEMICOLON, TokenType.RCURLYBRACKET);
						}
					}
					// throw another error in case our previous error handling consumed the last }
					notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
					consumeToken();

					program.addClassDeclaration(classDecl);
				}
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				consumeUntil(TokenType.RCURLYBRACKET);
			}
		}
		// No ClassDeclaration => Epsilon
		expect(TokenType.EOF);

		return program;
	}

	/**
	 * ClassMember' -> public MainMethod' | public Type IDENT ; | public Type IDENT ( Parameters? ) Block MainMethod' -> static void IDENT ( String [
	 * ] IDENT ) Block
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private MemberDeclaration parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			consumeToken();
			// Type
			if (isTokenType(TokenType.INT, TokenType.BOOLEAN, TokenType.VOID, TokenType.IDENTIFIER)) {
				Type type = parseType();

				Token firstToken = token;
				if (isTokenType(TokenType.IDENTIFIER)) {
					consumeToken();
					// public Type IDENT ;
					if (isTokenType(TokenType.SEMICOLON)) {
						consumeToken();
						// accept
						return new FieldDeclaration(firstToken.getPosition(), type, firstToken.getSymbol());
						// public Type IDENT ( Parameters? ) Block
					} else if (isTokenType(TokenType.LP)) {
						consumeToken();
						MethodDeclaration methDecl = new MethodDeclaration(firstToken.getPosition(), firstToken.getSymbol(), type);

						if (isTokenType(TokenType.RP)) {
							consumeToken();
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
			} else if (isTokenType(TokenType.STATIC)) {
				consumeToken();

				// save void return type
				Token retType = token;
				expectAndConsume(TokenType.VOID);

				// save identifier
				Token firstToken = token;
				expectAndConsume(TokenType.IDENTIFIER);
				expectAndConsume(TokenType.LP);

				expect(TokenType.IDENTIFIER);
				// identifier must be "String"
				if (!token.getSymbol().getValue().equals("String")) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				Position pos = token.getPosition();
				Symbol type = token.getSymbol();
				consumeToken();

				expectAndConsume(TokenType.LSQUAREBRACKET);
				expectAndConsume(TokenType.RSQUAREBRACKET);

				// save identifier symbol
				expect(TokenType.IDENTIFIER);
				Symbol ident = token.getSymbol();
				consumeToken();
				expectAndConsume(TokenType.RP);

				// new main method ast node
				ParameterDeclaration param = new ParameterDeclaration(pos, new ArrayType(pos, new ClassType(pos, type)), ident);
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
		if (isTokenType(TokenType.COMMA)) {
			consumeToken();
			parseParameters(methDecl);
		}
	}

	/**
	 * Parameter -> Type IDENT
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ParameterDeclaration parseParameter() throws ParserException, IOException {
		Type type = parseType();
		expect(TokenType.IDENTIFIER);
		ParameterDeclaration param = new ParameterDeclaration(token.getPosition(), type, token.getSymbol());
		consumeToken();
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
			consumeToken();
			break;
		case BOOLEAN:
			type = new Type(token.getPosition(), BasicType.BOOLEAN);
			consumeToken();
			break;
		case VOID:
			type = new Type(token.getPosition(), BasicType.VOID);
			consumeToken();
			break;
		case IDENTIFIER:
			type = new ClassType(token.getPosition(), token.getSymbol());
			consumeToken();
			break;
		default:
			throw new ParserException(token);
		}

		while (isTokenType(TokenType.LSQUAREBRACKET)) {
			consumeToken();
			expect(TokenType.RSQUAREBRACKET);
			Position pos = token.getPosition();
			consumeToken();

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
	private Statement parseStatement() throws IOException, ParserException {
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
			return parseIfStatement();
			// while statement
		case WHILE:
			return parseWhileStatement();
			// return statement
		case RETURN:
			return parseReturnStatement();
			// expression: propagate error recognition to parseExpressionStatement
		default:
			return parseExpressionStatement();
		}
	}

	/**
	 * Block -> { BlockStatement* }
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private Block parseBlock() throws ParserException, IOException {

		expect(TokenType.LCURLYBRACKET);
		Position pos = token.getPosition();
		Block block = new Block(pos);
		consumeToken();

		while (isNotTokenType(TokenType.RCURLYBRACKET)) {
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
				consumeUntil(TokenType.RCURLYBRACKET);
				// throw another error in case our previous error handling consumed the last }
				notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
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
				consumeUntil(TokenType.SEMICOLON, TokenType.RCURLYBRACKET);
				// throw another error in case our previous error handling consumed the last } or ;
				notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// while statement
		case WHILE:
			try {
				block.addStatement(parseWhileStatement());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				consumeUntil(TokenType.SEMICOLON, TokenType.RCURLYBRACKET);
				// throw another error in case our previous error handling consumed the last } or ;
				notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
			}
			break;
		// return statement
		case RETURN:
			try {
				block.addStatement(parseReturnStatement());
			} catch (ParserException e) {
				errorsDetected++;
				System.err.println(e);
				consumeUntil(TokenType.SEMICOLON);
				// throw another error in case our previous error handling consumed the last }
				notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
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
					consumeUntil(TokenType.SEMICOLON);
					// throw another error in case our previous error handling consumed the last ;
					notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
				}
				break;
			} else if (lookAhead.getType() == TokenType.LSQUAREBRACKET) {
				if (tokenSupplier.get2LookAhead().getType() == TokenType.RSQUAREBRACKET) {
					try {
						block.addStatement(parseLocalVariableDeclarationStatement());
					} catch (ParserException e) {
						errorsDetected++;
						System.err.println(e);
						consumeUntil(TokenType.SEMICOLON);
						// throw another error in case our previous error handling consumed the last ;
						notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
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
				consumeUntil(TokenType.SEMICOLON);
				// break on EOF
				notExpect(TokenType.EOF, TokenType.SEMICOLON);
				break;
			}
			expectAndConsume(TokenType.SEMICOLON);
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
		if (isTokenType(TokenType.ASSIGN)) {
			consumeToken();
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

		Statement statement = parseStatement();
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
		Statement trueStmt = parseStatement();

		if (isTokenType(TokenType.ELSE)) {
			consumeToken();
			Statement falseStmt = parseStatement();
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
		if (isNotTokenType(TokenType.SEMICOLON)) {
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
			Position position = token != null ? token.getPosition() : null;
			consumeToken();

			int precedence = operationTokenType.getPrecedence();
			if (operationTokenType.isLeftAssociative()) {
				precedence++;
			}

			Expression rhs = parseExpression(precedence);

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
			consumeToken();
			return new LogicalNotExpression(pos, parseUnaryExpression());
		case SUBTRACT:
			if (tokenSupplier.getLookAhead().getType() == TokenType.INTEGER) {
				return parsePostfixExpression();
			} else {
				pos = token.getPosition();
				consumeToken();
				return new NegateExpression(pos, parseUnaryExpression());
			}
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
		case SUBTRACT:
			Expression expr = parsePrimaryExpression();
			while (isTokenType(TokenType.LSQUAREBRACKET, TokenType.POINT)) {
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
		if (isTokenType(TokenType.POINT)) {
			consumeToken();
			Symbol symbol = token.getSymbol();
			if (isTokenType(TokenType.IDENTIFIER)) {
				consumeToken();
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
			consumeToken();
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
			consumeToken();
			Expression expr = parseExpression();
			if (isTokenType(TokenType.RSQUAREBRACKET)) {
				consumeToken();
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
			while (isTokenType(TokenType.COMMA)) {
				consumeToken();
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
			consumeToken();
			return new BooleanConstantExpression(pos, false);
		case TRUE:
			consumeToken();
			return new BooleanConstantExpression(pos, true);
		case SUBTRACT:
			consumeToken();
			symbol = token.getSymbol();
			consumeToken();
			return new IntegerConstantExpression(pos, symbol.getValue(), true);
		case INTEGER:
			consumeToken();
			return new IntegerConstantExpression(pos, symbol.getValue());
		case NULL:
			consumeToken();
			return new NullExpression(pos);
		case THIS:
			consumeToken();
			return new ThisExpression(pos);
		case IDENTIFIER:
			return parsePrimaryExpressionIdent();
		case LP:
			consumeToken();
			Expression expr = parseExpression();
			expectAndConsume(TokenType.RP);
			return expr;
		case NEW:
			consumeToken();
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
			consumeToken();
			if (isTokenType(TokenType.LP)) {
				consumeToken();
				Expression[] args = parseArguments();
				if (isTokenType(TokenType.RP)) {
					consumeToken();
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
			consumeToken();
			if (isTokenType(TokenType.LP)) {
				consumeToken();
				// new object
				if (isTokenType(TokenType.RP)) {
					consumeToken();
					return new NewObjectExpression(pos, symbol);
				} else {
					throw new ParserException(token, TokenType.RP);
				}
			} else if (isTokenType(TokenType.LSQUAREBRACKET)) {
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
		consumeToken();
		if (isTokenType(TokenType.LSQUAREBRACKET)) {
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
		if (isTokenType(TokenType.LSQUAREBRACKET)) {
			consumeToken();
			Expression expression = parseExpression();
			if (isTokenType(TokenType.RSQUAREBRACKET)) {
				consumeToken();
				ArrayType arrayType = new ArrayType(pos, type);
				while (token.getType() == TokenType.LSQUAREBRACKET) {
					Token lookahead = tokenSupplier.getLookAhead();
					if (lookahead.getType() == TokenType.RSQUAREBRACKET) {
						// read both
						consumeToken();
						consumeToken();
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
		consumeToken();
	}

	/**
	 * Checks if the next token has the given token type.
	 * 
	 * @param tokenType
	 *            not expected token
	 * @param expected
	 *            expected token
	 * @throws ParserException
	 *             if the token type does match
	 * @throws IOException
	 */
	private void notExpect(TokenType tokenType, TokenType expected) throws ParserException, IOException {
		if (token.getType() == tokenType) {
			throw new ParserException(token, expected);
		}
	}

	/**
	 * Checks if the next token has the given token type.
	 * 
	 * @param tokenType
	 * @return true if the token type matched, false otherwise
	 */
	private boolean isTokenType(TokenType tokenType) {
		if (token.getType() == tokenType) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the next token has not the given token type.
	 * 
	 * @param tokenType
	 * @return true if the token type did not match, false otherwise
	 */
	private boolean isNotTokenType(TokenType tokenType) {
		return !(isTokenType(tokenType));
	}

	/**
	 * Checks if the next token has one of the given token types.
	 * 
	 * @param tokenType
	 * @return true if the token type matched, false otherwise
	 */
	private boolean isTokenType(TokenType... tokenTypes) {
		for (TokenType type : tokenTypes) {
			if (token.getType() == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Consume the next token.
	 * 
	 * @throws IOException
	 */
	private void consumeToken() throws IOException {
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

	/**
	 * Consume tokens until one of the given tokens or EOF is read. The last read token is also consumed if it is not EOF.
	 * 
	 * @param tokenTypes
	 * @throws IOException
	 */
	private void consumeUntil(TokenType... tokenTypes) throws IOException {
		// never consume EOF
		while (isNotTokenType(TokenType.EOF)) {
			for (TokenType type : tokenTypes) {
				if (token.getType() == type) {
					// consume and return
					consumeToken();
					return;
				}
			}
			consumeToken();
		}

	}
}
