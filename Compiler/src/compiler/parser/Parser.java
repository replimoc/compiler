package compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import compiler.Symbol;
import compiler.ast.Block;
import compiler.ast.Program;
import compiler.ast.declaration.ClassDeclaration;
import compiler.ast.declaration.FieldDeclaration;
import compiler.ast.declaration.LocalVariableDeclaration;
import compiler.ast.declaration.MainMethodDeclaration;
import compiler.ast.declaration.MemberDeclaration;
import compiler.ast.declaration.MethodDeclaration;
import compiler.ast.declaration.NativeMethodDeclaration;
import compiler.ast.declaration.ParameterDeclaration;
import compiler.ast.statement.ArrayAccessExpression;
import compiler.ast.statement.BooleanConstantExpression;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.IntegerConstantExpression;
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
	private final List<ParserException> detectedErrors = new LinkedList<>();

	/**
	 * Current token.
	 */
	private Token token;
	private Program ast;

	public Parser(TokenSuppliable tokenSupplier) throws IOException {
		this.tokenSupplier = tokenSupplier;
		consumeToken();
	}

	private void recordError(ParserException exception) {
		detectedErrors.add(exception);
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
			recordError(e);
		}

		if (detectedErrors.isEmpty()) {
			return ast;
		} else {
			throw new ParsingFailedException(detectedErrors);
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
				ClassDeclaration classDecl = new ClassDeclaration(token.getPosition(), token.getSymbol());
				expectAndConsume(TokenType.IDENTIFIER);
				expectAndConsume(TokenType.LCURLYBRACKET);

				if (isTokenType(TokenType.RCURLYBRACKET)) {
					consumeToken();
					program.addClassDeclaration(classDecl);
				} else {
					// there is at least one ClassMember
					while (isNotTokenType(TokenType.RCURLYBRACKET) && isNotTokenType(TokenType.EOF)) {
						try {
							classDecl.addClassMember(parseClassMember());
						} catch (ParserException e) {
							recordError(e);
							consumeUntil(TokenType.SEMICOLON, TokenType.RCURLYBRACKET);
						}
					}
					// throw another error in case our previous error handling consumed the last }
					notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
					consumeToken();

					program.addClassDeclaration(classDecl);
				}
			} catch (ParserException e) {
				recordError(e);
				consumeUntil(TokenType.RCURLYBRACKET);
			}
		}
		// No ClassDeclaration => Epsilon
		expectAndConsume(TokenType.EOF);

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
		expectAndConsume(TokenType.PUBLIC);

		Token staticToken = null;

		if (isTokenType(TokenType.STATIC)) {
			staticToken = token;
			consumeToken();
		}

		Token nativeToken = null;
		if (isTokenType(TokenType.NATIVE)) {
			nativeToken = token;
			consumeToken();
		}

		switch (token.getType()) {
		case INT:
		case BOOLEAN:
		case VOID:
		case IDENTIFIER:
			return parseMemberDeclaration(staticToken, nativeToken);
		default:
			throw new ParserException(token);
		}
	}

	private MemberDeclaration parseMemberDeclaration(Token staticToken, Token nativeToken) throws IOException, ParserException {
		final boolean isStatic = staticToken != null;
		final boolean isNative = nativeToken != null;

		Type type = parseType();

		Token identifierToken = token;
		expectAndConsume(TokenType.IDENTIFIER);

		switch (token.getType()) {
		case SEMICOLON: // public (static)? (native)? Type IDENT ; => parse field
			if (isStatic) { // no static fields allowed.
				throw new ParserException(staticToken);
			}
			if (isNative) { // no native fields possible
				throw new ParserException(nativeToken);
			}

			consumeToken();
			return new FieldDeclaration(identifierToken.getPosition(), type, identifierToken.getSymbol());

		case LP: // public (static)? (native)? Type IDENT ( Parameters? ) Block
			consumeToken();

			Symbol identifier = identifierToken.getSymbol();

			List<ParameterDeclaration> parameters = parseParameters();

			if (isStatic && "main".equals(identifier.getValue())) {
				if (isNative) {
					throw new ParserException(nativeToken);
				}

				return new MainMethodDeclaration(identifierToken.getPosition(), identifier, parameters, type, parseBlock());
			} else if (isNative) {
				isTokenType(TokenType.SEMICOLON);
				consumeToken();
				return new NativeMethodDeclaration(identifierToken.getPosition(), isStatic, identifier, parameters, type);
			} else {
				return new MethodDeclaration(identifierToken.getPosition(), isStatic, identifier, parameters, type, parseBlock());
			}
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * Parameters -> Parameter (, Parameters)?
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private List<ParameterDeclaration> parseParameters() throws IOException, ParserException {
		List<ParameterDeclaration> parameters = new LinkedList<ParameterDeclaration>();

		switch (token.getType()) {
		case RP:
			consumeToken();
			break;
		default:
			parameters.add(parseParameter());
			while (isTokenType(TokenType.COMMA)) {
				consumeToken();
				parameters.add(parseParameter());
			}
			expectAndConsume(TokenType.RP);
			break;
		}

		return parameters;
	}

	/**
	 * Parameter -> Type IDENT
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ParameterDeclaration parseParameter() throws ParserException, IOException {
		Type type = parseType();
		ParameterDeclaration param = new ParameterDeclaration(token.getPosition(), type, token.getSymbol());
		expectAndConsume(TokenType.IDENTIFIER);
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
			Position pos = token.getPosition();
			expectAndConsume(TokenType.RSQUAREBRACKET);

			type = new ArrayType(pos, type);
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

		Position pos = token.getPosition();
		expectAndConsume(TokenType.LCURLYBRACKET);
		Block block = new Block(pos);

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
				recordError(e);
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
				recordError(e);
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
				recordError(e);
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
				recordError(e);
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
			switch (lookAhead.getType()) {
			case IDENTIFIER:
				try {
					block.addStatement(parseLocalVariableDeclarationStatement());
				} catch (ParserException e) {
					recordError(e);
					consumeUntil(TokenType.SEMICOLON);
					// throw another error in case our previous error handling consumed the last ;
					notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
				}
				return;
			case LSQUAREBRACKET: // TODO: Implement as switch-case
				if (tokenSupplier.get2LookAhead().getType() == TokenType.RSQUAREBRACKET) {
					try {
						block.addStatement(parseLocalVariableDeclarationStatement());
					} catch (ParserException e) {
						recordError(e);
						consumeUntil(TokenType.SEMICOLON);
						// throw another error in case our previous error handling consumed the last ;
						notExpect(TokenType.EOF, TokenType.RCURLYBRACKET);
					}
					return;
				}
				break;
			default:
				break;
			}
			// expression: fall through to outer default case
		default:
			try {
				block.addStatement(parseExpression());
			} catch (ParserException e) {
				recordError(e);
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
			switch (tokenSupplier.getLookAhead().getType()) {
			case INTEGER:
				return parsePostfixExpression();
			default:
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
			while (isTokenType(TokenType.LSQUAREBRACKET) || isTokenType(TokenType.POINT)) {
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
		expectAndConsume(TokenType.POINT);
		Symbol symbol = token.getSymbol();
		expectAndConsume(TokenType.IDENTIFIER);
		return parseMethodInvocationFieldAccess(leftExpression, symbol);
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
		expectAndConsume(TokenType.LSQUAREBRACKET);
		Expression expr = parseExpression();
		expectAndConsume(TokenType.RSQUAREBRACKET);
		return expr;
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

		Position pos = token.getPosition();
		Symbol symbol = token.getSymbol();
		expectAndConsume(TokenType.IDENTIFIER);

		if (isTokenType(TokenType.LP)) {
			consumeToken();
			Expression[] args = parseArguments();
			expectAndConsume(TokenType.RP);
			// no access object
			return new MethodInvocationExpression(pos, null, symbol, args);
		}
		// assume "PrimaryIdent -> IDENT" when another token than '(' is
		// read
		return new VariableAccessExpression(pos, null, symbol);
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
			switch (token.getType()) {
			case LP:
				consumeToken();
				// new object
				expectAndConsume(TokenType.RP);
				return new NewObjectExpression(pos, symbol);
			case LSQUAREBRACKET:
				// new array
				type = new ClassType(pos, symbol);
				return parseNewArrayExpressionHelp(pos, type);
			default:
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
		return parseNewArrayExpressionHelp(pos, type);
	}

	/**
	 * NewArrayExpression -> [Expression] ([])*
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private NewArrayExpression parseNewArrayExpressionHelp(Position pos, Type type) throws IOException,
			ParserException {
		expectAndConsume(TokenType.LSQUAREBRACKET);
		Expression expression = parseExpression();
		expectAndConsume(TokenType.RSQUAREBRACKET);
		ArrayType arrayType = new ArrayType(pos, type);
		while (token.getType() == TokenType.LSQUAREBRACKET) {
			Token lookahead = tokenSupplier.getLookAhead();
			switch (lookahead.getType()) {
			case RSQUAREBRACKET:
				// read both
				consumeToken();
				consumeToken();
				arrayType = new ArrayType(pos, arrayType);
				break;
			default:
				// read [, but not part of NewArrayExpression
				// could be [Expression]ArrayAccess
				// [Expression][Expression]
				return new NewArrayExpression(pos, arrayType, expression);
			}
		}
		return new NewArrayExpression(pos, arrayType, expression);
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
		if (token.getType() != tokenType) {
			throw new ParserException(token, tokenType);
		}
		token = tokenSupplier.getNextToken();
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
	 * Consume the next token.
	 * 
	 * @throws IOException
	 */
	private void consumeToken() throws IOException {
		token = tokenSupplier.getNextToken();
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
