package compiler.parser;

import java.io.IOException;

import compiler.ast.statement.Expression;
import compiler.ast.statement.VariableAccessExpression;
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
			parseProgram();
		} catch (ParserException e) {
			errorsDetected++;
			System.err.println(e);
		}
		return errorsDetected;
	}

	/**
	 * Program -> epsilon | class IDENT { ClassMember' } Program
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseProgram() throws IOException, ParserException {
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
				token = tokenSupplier.getNextToken();

				if (token.getType() != TokenType.LCURLYBRACKET) {
					throw new ParserException(token, TokenType.LCURLYBRACKET);
				}
				token = tokenSupplier.getNextToken();

				if (token.getType() == TokenType.RCURLYBRACKET) {
					token = tokenSupplier.getNextToken();
					continue;
				} else {
					while (token.getType() != TokenType.RCURLYBRACKET && token.getType() != TokenType.EOF) {
						try {
							parseClassMember();
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
	}

	/**
	 * ClassMember' -> public MainMethod' | public Type IDENT ; | public Type IDENT ( Parameters? ) Block MainMethod' -> static void IDENT ( String [
	 * ] IDENT ) Block
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			token = tokenSupplier.getNextToken();
			// Type
			if (token.getType() == TokenType.INT
					|| token.getType() == TokenType.BOOLEAN
					|| token.getType() == TokenType.VOID
					|| token.getType() == TokenType.IDENTIFIER) {
				parseType();

				if (token.getType() == TokenType.IDENTIFIER) {
					token = tokenSupplier.getNextToken();
					// public Type IDENT ;
					if (token.getType() == TokenType.SEMICOLON) {
						// accept
						token = tokenSupplier.getNextToken();
						break;
						// public Type IDENT ( Parameters? ) Block
					} else if (token.getType() == TokenType.LP) {
						token = tokenSupplier.getNextToken();

						if (token.getType() == TokenType.RP) {
							token = tokenSupplier.getNextToken();
						} else {
							parseParameters();
							if (token.getType() != TokenType.RP) {
								throw new ParserException(token, TokenType.RP);
							}
							token = tokenSupplier.getNextToken();
						}
						parseBlock();
						break;
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
				token = tokenSupplier.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token, TokenType.IDENTIFIER);
				}
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
				parseBlock();
				break;
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
	private void parseParameters() throws IOException, ParserException {
		parseParameter();
		if (token.getType() == TokenType.COMMA) {
			token = tokenSupplier.getNextToken();
			parseParameters();
		}
	}

	/**
	 * Parameter -> Type IDENT
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseParameter() throws ParserException, IOException {
		parseType();
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token, TokenType.IDENTIFIER);
		}
		token = tokenSupplier.getNextToken();
	}

	/**
	 * Type -> Basictype ([])
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseType() throws IOException, ParserException {
		switch (token.getType()) {
		case INT:
		case BOOLEAN:
		case VOID:
		case IDENTIFIER:
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
			token = tokenSupplier.getNextToken();
		}
	}

	/**
	 * Statement -> Block | EmptyStatement | IfStatement | ExpressionStatement | WhileStatement | ReturnStatement
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseStatement() throws IOException, ParserException {
		switch (token.getType()) {
		// block
		case LCURLYBRACKET:
			parseBlock();
			break;
		// empty statement
		case SEMICOLON:
			parseEmptyStatement();
			break;
		// if statement
		case IF:
			parseIfStatement();
			break;
		// while statement
		case WHILE:
			parseWhileStatement();
			break;
		// return statement
		case RETURN:
			parseReturnStatement();
			break;
		// expression: propagate error recognition to parseExpressionStatement
		default:
			parseExpressionStatement();
		}
	}

	/**
	 * Block -> { BlockStatement* }
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseBlock() throws ParserException, IOException {
		if (token.getType() != TokenType.LCURLYBRACKET) {
			throw new ParserException(token, TokenType.LCURLYBRACKET);
		}
		token = tokenSupplier.getNextToken();

		while (token.getType() != TokenType.RCURLYBRACKET) {
			parseBlockStatement();
		}

		if (token.getType() != TokenType.RCURLYBRACKET) {
			throw new ParserException(token, TokenType.RCURLYBRACKET);
		}
		token = tokenSupplier.getNextToken();
	}

	/**
	 * BlockStatement -> Block | EmptyStatement | IfStatement | ExpressionStatement | WhileStatement | ReturnStatement |
	 * LocalVariableDeclarationStatement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseBlockStatement() throws ParserException, IOException {
		switch (token.getType()) {
		// block
		case LCURLYBRACKET:
			try {
				parseBlock();
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
				parseIfStatement();
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
				parseWhileStatement();
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
				parseReturnStatement();
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
			parseLocalVariableDeclarationStatement();
			break;
		case IDENTIFIER:
			// get 2 tokens look ahead
			Token lookAhead = tokenSupplier.getLookAhead();
			if (lookAhead.getType() == TokenType.IDENTIFIER) {
				try {
					parseLocalVariableDeclarationStatement();
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
						parseLocalVariableDeclarationStatement();
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
				parseExpression();
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
	private void parseLocalVariableDeclarationStatement() throws IOException,
			ParserException {
		parseType();
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token, TokenType.IDENTIFIER);
		}
		token = tokenSupplier.getNextToken();

		if (token.getType() == TokenType.ASSIGN) {
			token = tokenSupplier.getNextToken();
			parseExpression();
		}

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
	}

	/**
	 * EmptyStatement -> ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseEmptyStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
	}

	/**
	 * WhileStatement-> while ( Expression ) Statement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseWhileStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.WHILE) {
			throw new ParserException(token, TokenType.WHILE);
		}
		token = tokenSupplier.getNextToken();

		if (token.getType() != TokenType.LP) {
			throw new ParserException(token, TokenType.LP);
		}
		token = tokenSupplier.getNextToken();

		parseExpression();

		if (token.getType() != TokenType.RP) {
			throw new ParserException(token, TokenType.RP);
		}
		token = tokenSupplier.getNextToken();

		parseStatement();
	}

	/**
	 * IfStatement -> if ( Expression ) Statement (else Statement)?
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseIfStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.IF) {
			throw new ParserException(token, TokenType.IF);
		}
		token = tokenSupplier.getNextToken();

		if (token.getType() != TokenType.LP) {
			throw new ParserException(token, TokenType.LP);
		}
		token = tokenSupplier.getNextToken();

		parseExpression();

		if (token.getType() != TokenType.RP) {
			throw new ParserException(token, TokenType.RP);
		}
		token = tokenSupplier.getNextToken();

		parseStatement();

		if (token.getType() == TokenType.ELSE) {
			token = tokenSupplier.getNextToken();
			parseStatement();
		}
	}

	/**
	 * ExpressionStatement -> Expression ;
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseExpressionStatement() throws IOException, ParserException {
		parseExpression();

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
	}

	/**
	 * ReturnStatement -> return Expression? ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseReturnStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.RETURN) {
			throw new ParserException(token, TokenType.RETURN);
		}
		token = tokenSupplier.getNextToken();

		if (token.getType() != TokenType.SEMICOLON) {
			parseExpression();
		}

		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token, TokenType.SEMICOLON);
		}
		token = tokenSupplier.getNextToken();
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
		return new VariableAccessExpression(token.getPosition(), null); // FIXME This should be another value;
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
