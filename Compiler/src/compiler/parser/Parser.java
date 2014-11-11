package compiler.parser;

import java.io.IOException;

import compiler.ast.Block;
import compiler.ast.ClassDeclaration;
import compiler.ast.ClassMember;
import compiler.ast.FieldDeclaration;
import compiler.ast.MethodDeclaration;
import compiler.ast.ParameterDefinition;
import compiler.ast.Program;
import compiler.ast.statement.Expression;
import compiler.ast.statement.IfStatement;
import compiler.ast.statement.LocalVariableDeclaration;
import compiler.ast.statement.Statement;
import compiler.ast.statement.WhileStatement;
import compiler.ast.statement.type.ArrayType;
import compiler.ast.statement.type.BasicType;
import compiler.ast.statement.type.ClassType;
import compiler.ast.statement.type.Type;
import compiler.ast.statement.unary.ReturnStatement;
import compiler.lexer.OperationType;
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
	 */
	public int parse() throws IOException {
		try {
			ast = parseProgram();
		} catch (ParserException e) {
			errorsDetected++;
			System.err.println(e);
		}
		return errorsDetected;
	}
	
	public Program getAST() {
		return ast;
	}

	/**
	 * Program -> epsilon | class IDENT { ClassMember' } Program
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Program parseProgram() throws IOException, ParserException {
		Program program = new Program();
		
		// ClassDeclaration*
		while (token.getType() != TokenType.EOF) {
			try {
				if (token.getType() != TokenType.CLASS) {
					throw new ParserException(token, TokenType.CLASS);
				}
				token = tokenSupplier.getNextToken();

				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				ClassDeclaration classDecl = new ClassDeclaration(token.getPosition(), token.getSymbol());
				token = tokenSupplier.getNextToken();

				if (token.getType() != TokenType.LCURLYBRACKET) {
					throw new ParserException(token, TokenType.LCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();

				if (token.getType() == TokenType.RCURLYBRACKET) {
					token = tokenSupplier.getNextToken();
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
					// throw another error in case our previous error handling consumed the last } or ;
					if (token.getType() == TokenType.EOF) {
						throw new ParserException(token, TokenType.RCURLYBRACKET);
					}
					token = tokenSupplier.getNextToken();
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
				if (token.getType() == TokenType.IDENTIFIER) {
					token = tokenSupplier.getNextToken();
					// public Type IDENT ;
					if (token.getType() == TokenType.SEMICOLON) {
						// accept
						token = tokenSupplier.getNextToken();
						return new FieldDeclaration(firstToken.getPosition(), type, firstToken.getSymbol());
						// public Type IDENT ( Parameters? ) Block
					} else if (token.getType() == TokenType.LP) {
						token = tokenSupplier.getNextToken();
						MethodDeclaration methDecl = new MethodDeclaration(firstToken.getPosition(), firstToken.getSymbol(), type);

						if (token.getType() == TokenType.RP) {
							token = tokenSupplier.getNextToken();
						} else {
							parseParameters(methDecl);
							if (token.getType() != TokenType.RP) {
								throw new ParserException(token, TokenType.RP);
							}
							token = tokenSupplier.getNextToken();
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
			} else if (token.getType() == TokenType.STATIC) {
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.VOID) {
					throw new ParserException(token, TokenType.VOID);
				}
				Token retType = token;
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				Token firstToken = token;
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.LP) {
					throw new ParserException(token, TokenType.LP);
				}
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER || !token.getSymbol().getValue().equals("String")) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.LSQUAREBRACKET) {
					throw new ParserException(token, TokenType.LSQUAREBRACKET);
				}
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.RSQUAREBRACKET) {
					throw new ParserException(token, TokenType.RSQUAREBRACKET);
				}
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.RP) {
					throw new ParserException(token, TokenType.RP);
				}
				token = tokenSupplier.getNextToken();
				return new MethodDeclaration(firstToken.getPosition(), firstToken.getSymbol(), new Type(retType.getPosition(), BasicType.VOID), parseBlock());
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
		if (token.getType() == TokenType.COMMA) {
			token = tokenSupplier.getNextToken();
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
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token, TokenType.IDENTIFIER);
		}
		ParameterDefinition param = new ParameterDefinition(token.getPosition(), type, token.getSymbol());
		token = tokenSupplier.getNextToken();
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

		while (token.getType() == TokenType.LSQUAREBRACKET) {
			token = tokenSupplier.getNextToken();

			if (token.getType() != TokenType.RSQUAREBRACKET) {
				throw new ParserException(token, TokenType.RSQUAREBRACKET);
			}
			type = new ArrayType(token.getPosition(), type); //TODO: seems to be ugly
			token = tokenSupplier.getNextToken();
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
			return parseEmptyStatement();
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
		if (token.getType() != TokenType.LCURLYBRACKET) {
			throw new ParserException(token, TokenType.LCURLYBRACKET);
		}
		Block block = new Block(token.getPosition());
		token = tokenSupplier.getNextToken();

		while (token.getType() != TokenType.RCURLYBRACKET) {
			parseBlockStatement(block);
		}

		if (token.getType() != TokenType.RCURLYBRACKET) {
			throw new ParserException(token, TokenType.RCURLYBRACKET);
		}
		token = tokenSupplier.getNextToken();
		
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
				if (token.getType() == TokenType.EOF) {
					throw new ParserException(token, TokenType.RCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();
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
				// throw another error in case our previous error handling consumed the last } or ;
				if (token.getType() == TokenType.EOF) {
					throw new ParserException(token, TokenType.RCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();
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
				// throw another error in case our previous error handling consumed the last } or ;
				if (token.getType() == TokenType.EOF) {
					throw new ParserException(token, TokenType.RCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();
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
				// throw another error in case our previous error handling consumed the last ;
				if (token.getType() == TokenType.EOF) {
					throw new ParserException(token, TokenType.RCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();
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
					// throw another error in case our previous error handling consumed the last ;
					if (token.getType() == TokenType.EOF) {
						throw new ParserException(token, TokenType.RCURLYBRACKET);
					}
					token = tokenSupplier.getNextToken();
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
						// throw another error in case our previous error handling consumed the last ;
						if (token.getType() == TokenType.EOF) {
							throw new ParserException(token, TokenType.RCURLYBRACKET);
						}
						token = tokenSupplier.getNextToken();
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
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token, TokenType.IDENTIFIER);
		}
		token = tokenSupplier.getNextToken();

		Expression expr = null;
		if (token.getType() == TokenType.ASSIGN) {
			token = tokenSupplier.getNextToken();
			expr = parseExpression();
		}

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
		return expr == null ? new LocalVariableDeclaration(firstToken.getPosition(), type, firstToken.getSymbol()) : 
			new LocalVariableDeclaration(firstToken.getPosition(), type, firstToken.getSymbol(), expr);
	}

	/**
	 * EmptyStatement -> ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private Statement parseEmptyStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
		return null;
	}

	/**
	 * WhileStatement-> while ( Expression ) Statement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private WhileStatement parseWhileStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.WHILE) {
			throw new ParserException(token, TokenType.WHILE);
		}
		Token firstToken = token;
		token = tokenSupplier.getNextToken();

		if (token.getType() != TokenType.LP) {
			throw new ParserException(token, TokenType.LP);
		}
		token = tokenSupplier.getNextToken();

		Expression expr = parseExpression();

		if (token.getType() != TokenType.RP) {
			throw new ParserException(token, TokenType.RP);
		}
		token = tokenSupplier.getNextToken();

		Statement stmt = parseStatement();
		return new WhileStatement(firstToken.getPosition(), expr, stmt);
	}

	/**
	 * IfStatement -> if ( Expression ) Statement (else Statement)?
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private IfStatement parseIfStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.IF) {
			throw new ParserException(token, TokenType.IF);
		}
		Token firstToken = token;
		token = tokenSupplier.getNextToken();

		if (token.getType() != TokenType.LP) {
			throw new ParserException(token, TokenType.LP);
		}
		token = tokenSupplier.getNextToken();

		Expression expr = parseExpression();

		if (token.getType() != TokenType.RP) {
			throw new ParserException(token, TokenType.RP);
		}
		token = tokenSupplier.getNextToken();

		Statement trueStmt = parseStatement();

		if (token.getType() == TokenType.ELSE) {
			token = tokenSupplier.getNextToken();
			Statement falseStmt = parseStatement();
			return new IfStatement(firstToken.getPosition(), expr, trueStmt, falseStmt);
		}
		return new IfStatement(firstToken.getPosition(), expr, trueStmt);
	}

	/**
	 * ExpressionStatement -> Expression ;
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private Expression parseExpressionStatement() throws IOException, ParserException {
		Expression expr = parseExpression();

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
		return expr;
	}

	/**
	 * ReturnStatement -> return Expression? ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private ReturnStatement parseReturnStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.RETURN) {
			throw new ParserException(token, TokenType.RETURN);
		}
		Token firstToken = token;
		token = tokenSupplier.getNextToken();

		Expression expr = null;
		if (token.getType() != TokenType.SEMICOLON) {
			expr = parseExpression();
		}

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
		return new ReturnStatement(firstToken.getPosition(), expr);
	}

	private Expression parseExpression() throws IOException, ParserException {
		parseExpression(0);
		return null; //TODO: :)
	}

	private String parseExpression(int minPrecedence) throws IOException, ParserException {
		String result = "_"; // This should assign the next line
		parseUnaryExpression();

		while (token != null && token.getType().getOperationType() == OperationType.BINARY &&
				token.getType().getPrecedence() >= minPrecedence) {
			Token operationToken = token;
			TokenType operationTokenType = operationToken.getType();
			token = tokenSupplier.getNextToken();

			int precedence = operationTokenType.getPrecedence();
			if (operationTokenType.isLeftAssociative()) {
				precedence++;
			}

			String rhs = parseExpression(precedence);

			switch (operationTokenType) {
			case NOTEQUAL:
				result = "(" + result + "!=" + rhs + ")";
				break;
			case MULTIPLY:
				result = "(" + result + "*" + rhs + ")";
				break;
			case ADD:
				result = "(" + result + "+" + rhs + ")";
				break;
			case SUBTRACT:
				result = "(" + result + "-" + rhs + ")";
				break;
			case DIVIDE:
				result = "(" + result + "/" + rhs + ")";
				break;
			case LESSEQUAL:
				result = "(" + result + "<=" + rhs + ")";
				break;
			case LESS:
				result = "(" + result + "<" + rhs + ")";
				break;
			case EQUAL:
				result = "(" + result + "==" + rhs + ")";
				break;
			case ASSIGN:
				result = "(" + result + "=" + rhs + ")";
				break;
			case GREATEREQUAL:
				result = "(" + result + ">=" + rhs + ")";
				break;
			case GREATER:
				result = "(" + result + ">" + rhs + ")";
				break;
			case MODULO:
				result = "(" + result + "%" + rhs + ")";
				break;
			case LOGICALAND:
				result = "(" + result + "&&" + rhs + ")";
				break;
			case LOGICALOR:
				result = "(" + result + "||" + rhs + ")";
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
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseUnaryExpression() throws IOException, ParserException {
		switch (token.getType()) {
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case LP:
		case NEW:
			parsePostfixExpression();
			break;
		case LOGICALNOT:
		case SUBTRACT:
			token = tokenSupplier.getNextToken();
			parseUnaryExpression();
			break;
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
	private void parsePostfixExpression() throws IOException, ParserException {
		switch (token.getType()) {
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case LP:
		case NEW:
			parsePrimaryExpression();
			while (token.getType() == TokenType.LSQUAREBRACKET
					|| token.getType() == TokenType.POINT) {
				parsePostfixOp();
			}
			break;
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
	private void parsePostfixOp() throws IOException, ParserException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			parseArrayAccess();
			break;
		case POINT:
			parsePostfixOpMethodField();
			break;
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
	private void parsePostfixOpMethodField() throws IOException,
			ParserException {
		if (token.getType() == TokenType.POINT) {
			token = tokenSupplier.getNextToken();
			if (token.getType() == TokenType.IDENTIFIER) {
				token = tokenSupplier.getNextToken();
				parseMethodInvocationFieldAccess();
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
	private void parseMethodInvocationFieldAccess() throws IOException, ParserException {
		switch (token.getType()) {
		case LP:
			// method invocation
			token = tokenSupplier.getNextToken();
			parseArguments();
			if (token.getType() != TokenType.RP)
				throw new ParserException(token, TokenType.RP);
			token = tokenSupplier.getNextToken();
			break;
		default:
		}
	}

	/**
	 * ArrayAccess -> [ Expression ]
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseArrayAccess() throws IOException, ParserException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			token = tokenSupplier.getNextToken();
			parseExpression();
			if (token.getType() == TokenType.RSQUAREBRACKET) {
				token = tokenSupplier.getNextToken();
				break;
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
	private void parseArguments() throws IOException, ParserException {
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
			parseExpression();
			while (token.getType() == TokenType.COMMA) {
				token = tokenSupplier.getNextToken();
				parseExpression();
			}
			break;
		// no expression
		case RP:
		default:
		}
	}

	/**
	 * PrimaryExpression -> null | false | true | INTEGER_LITERAL | PrimaryIdent | this | ( Expression ) | new NewExpression
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parsePrimaryExpression() throws IOException, ParserException {
		switch (token.getType()) {
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
			token = tokenSupplier.getNextToken();
			break;
		case IDENTIFIER:
			parsePrimaryExpressionIdent();
			break;
		case LP:
			token = tokenSupplier.getNextToken();
			parseExpression();
			if (token.getType() != TokenType.RP)
				throw new ParserException(token, TokenType.RP);
			token = tokenSupplier.getNextToken();
			break;
		case NEW:
			token = tokenSupplier.getNextToken();
			parseNewExpression();
			break;
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
	private void parsePrimaryExpressionIdent() throws IOException,
			ParserException {
		switch (token.getType()) {
		case IDENTIFIER:
			token = tokenSupplier.getNextToken();
			if (token.getType() == TokenType.LP) {
				token = tokenSupplier.getNextToken();
				parseArguments();
				if (token.getType() == TokenType.RP) {
					token = tokenSupplier.getNextToken();
					return;
				} else {
					throw new ParserException(token, TokenType.RP);
				}
			}
			// assume "PrimaryIdent -> IDENT" when another token than '(' is
			// read
			break;
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
	private void parseNewExpression() throws IOException, ParserException {
		switch (token.getType()) {
		case IDENTIFIER:
			// new object or new array
			token = tokenSupplier.getNextToken();
			if (token.getType() == TokenType.LP) {
				// new object
				token = tokenSupplier.getNextToken();
				if (token.getType() == TokenType.RP) {
					token = tokenSupplier.getNextToken();
					return;
				} else {
					throw new ParserException(token, TokenType.RP);
				}
			} else if (token.getType() == TokenType.LSQUAREBRACKET) {
				// new array
				parseNewArrayExpressionHelp();
			} else {
				throw new ParserException(token);
			}
			break;
		case INT:
		case BOOLEAN:
		case VOID:
			// new array
			token = tokenSupplier.getNextToken();
			if (token.getType() == TokenType.LSQUAREBRACKET) {
				// new array
				parseNewArrayExpressionHelp();
			} else {
				throw new ParserException(token, TokenType.LSQUAREBRACKET);
			}
			break;
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * NewArrayExpression -> [Expression] ([])*
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseNewArrayExpressionHelp() throws IOException,
			ParserException {
		if (token.getType() == TokenType.LSQUAREBRACKET) {
			token = tokenSupplier.getNextToken();
			parseExpression();
			if (token.getType() == TokenType.RSQUAREBRACKET) {
				token = tokenSupplier.getNextToken();
				while (token.getType() == TokenType.LSQUAREBRACKET) {
					Token lookahead = tokenSupplier.getLookAhead();
					if (lookahead.getType() == TokenType.RSQUAREBRACKET) {
						// read both
						token = tokenSupplier.getNextToken();
						token = tokenSupplier.getNextToken();
					} else {
						// read [, but not part of NewArrayExpression
						// could be [Expression]ArrayAccess
						// [Expression][Expression]
						return;
					}
				}
			} else {
				throw new ParserException(token, TokenType.RSQUAREBRACKET);
			}
		} else {
			throw new ParserException(token, TokenType.LSQUAREBRACKET);
		}
	}
}
