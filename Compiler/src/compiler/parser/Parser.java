package compiler.parser;

import java.io.IOException;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class Parser {

	private final Lexer lexer;
	/**
	 * Current token.
	 */
	private Token token;

	public Parser(Lexer lexer) throws IOException {
		this.lexer = lexer;
		token = lexer.getNextToken();
	}

	public void parse() throws IOException, ParserException {
		parseProgram();
	}

	/**
	 * Program -> epsilon | class IDENT { ClassMember' } Program
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parseProgram() throws IOException, ParserException {
		// ClassDeclaration*
		while (token.getType() == TokenType.CLASS) {
			token = lexer.getNextToken();

			if (token.getType() != TokenType.IDENTIFIER) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();

			if (token.getType() != TokenType.LCURLYBRACKET) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();

			if (token.getType() == TokenType.RCURLYBRACKET) {
				token = lexer.getNextToken();
				continue;
			} else {
				parseClassMember();
				if (token.getType() != TokenType.RCURLYBRACKET) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();

				continue;
			}
		}

		// No ClassDeclaration => Epsilon
		if (token.getType() != TokenType.EOF) {
			throw new ParserException(token);
		}
	}

	/**
	 * ClassMember' -> public MainMethod' | public Type IDENT ; | public Type IDENT ( Parameters? ) Block
	 * MainMethod' -> static void IDENT ( String [ ] IDENT ) Block
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			token = lexer.getNextToken();
			// Type
			if (token.getType() == TokenType.INT
					|| token.getType() == TokenType.BOOLEAN
					|| token.getType() == TokenType.VOID
					|| token.getType() == TokenType.IDENTIFIER) {
				token = lexer.getNextToken();
				
				if (token.getType() == TokenType.IDENTIFIER) {
					token = lexer.getNextToken();
					// public Type IDENT ;
					if (token.getType() == TokenType.SEMICOLON) {
						// accept
						token = lexer.getNextToken();
						break;
					// public Type IDENT ( Parameters? ) Block
					} else if (token.getType() == TokenType.LP) {
						token = lexer.getNextToken();

						if (token.getType() == TokenType.RP) {
							token = lexer.getNextToken();
						} else {
							parseParameters();
							if (token.getType() != TokenType.RP) {
								throw new ParserException(token);
							}
							token = lexer.getNextToken();
						}
						parseBlock();
						break;
					}
				}
			// MainMethod' -> static void IDENT ( String [ ] IDENT ) Block
			} else if (token.getType() == TokenType.STATIC) {
				token = lexer.getNextToken();
				if (token.getType() != TokenType.VOID) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.LP) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER
						|| token.getSymbol().getValue().equals("String") == false) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.LSQUAREBRACKET) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.RSQUAREBRACKET) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.IDENTIFIER) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				if (token.getType() != TokenType.RP) {
					throw new ParserException(token);
				}
				token = lexer.getNextToken();
				parseBlock();
				break;
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
	private void parseParameters() throws IOException, ParserException {
		parseParameter();
		if (token.getType() == TokenType.COMMA) {
			token = lexer.getNextToken();
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
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
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
			token = lexer.getNextToken();
			break;
		default:
			throw new ParserException(token);
		}

		while (token.getType() == TokenType.LSQUAREBRACKET) {
			token = lexer.getNextToken();

			if (token.getType() != TokenType.RSQUAREBRACKET) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();
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
		default :
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
			throw new ParserException(token);
		}
		token = lexer.getNextToken();

		while (token.getType() != TokenType.RCURLYBRACKET) {
			parseBlockStatement();
		}

		if (token.getType() != TokenType.RCURLYBRACKET) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	/**
	 * BlockStatement -> Block | EmptyStatement | IfStatement | ExpressionStatement | WhileStatement | ReturnStatement | LocalVariableDeclarationStatement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseBlockStatement() throws ParserException, IOException {
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
		// ExpressionStatement or LocalVariableDeclarationStatement
		case INT:
		case BOOLEAN:
		case VOID:
			parseLocalVariableDeclarationStatement();
			break;
		case IDENTIFIER:
			if (lexer.getLookAhead().getType() == TokenType.IDENTIFIER) {
				parseLocalVariableDeclarationStatement();
				break;
			}
		default:
			parseExpression();
			if (token.getType() != TokenType.SEMICOLON) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();
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
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		if (token.getType() == TokenType.EQUAL) {
			token = lexer.getNextToken();
			parseExpression();
		}
		token = lexer.getNextToken();
		
		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	/**
	 * EmptyStatement -> ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseEmptyStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	/**
	 * WhileStatement-> while ( Expression ) Statement
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseWhileStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.WHILE) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		if (token.getType() != TokenType.LP) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		parseExpression();
		
		if (token.getType() != TokenType.RP) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
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
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		if (token.getType() != TokenType.LP) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		parseExpression();
		
		if (token.getType() != TokenType.RP) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		parseStatement();
		
		if (token.getType() == TokenType.ELSE) {
			token = lexer.getNextToken();
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
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	/**
	 * ReturnStatement -> return Expression? ;
	 * 
	 * @throws ParserException
	 * @throws IOException
	 */
	private void parseReturnStatement() throws ParserException, IOException {
		if (token.getType() != TokenType.RETURN) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
		
		//parseExpression();
		
		if (token.getType() != TokenType.SEMICOLON) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	private void parseExpression() {
		// precedence climbing
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
			token = lexer.getNextToken();
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
			token = lexer.getNextToken();
			if (token.getType() == TokenType.IDENTIFIER) {
				token = lexer.getNextToken();
				parseMethodInvocation();
			} else {
				throw new ParserException(token);
			}
		} else {
			throw new ParserException(token);
		}
	}

	/**
	 * MethodInvocationFieldAccess' -> ( Arguments ) | epsilon
	 * 
	 * @throws IOException
	 */
	private void parseMethodInvocation() throws IOException {
		switch (token.getType()) {
		case LP:
			// method invocation
			token = lexer.getNextToken();
			parseArguments();
			break;
		default:
			// field access
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
			token = lexer.getNextToken();
			parseExpression();
			if (token.getType() == TokenType.RSQUAREBRACKET) {
				token = lexer.getNextToken();
				break;
			} else {
				throw new ParserException(token);
			}
		default:
			throw new ParserException(token);
		}
	}

	/**
	 * Arguments -> (Expression (, Expression)*)?
	 * 
	 * @throws IOException
	 */
	private void parseArguments() throws IOException {
		switch (token.getType()) {
		case LOGICALNOT:
		case SUBTRACT:
		case NULL:
		case FALSE:
		case TRUE:
		case INTEGER:
		case THIS:
		case IDENTIFIER:
		case LP:
		case NEW:
			parseExpression();
			while (token.getType() == TokenType.COMMA) {
				token = lexer.getNextToken();
				parseExpression();
			}
			break;
		default:
			// allowed
		}
	}

	/**
	 * PrimaryExpression -> null | false | true | INTEGER_LITERAL | PrimaryIdent
	 * | this | ( Expression ) | new NewExpression
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
			token = lexer.getNextToken();
			break;
		case IDENTIFIER:
			parsePrimaryExpressionIdent();
			break;
		case LP:
			parseExpression();
			break;
		case NEW:
			token = lexer.getNextToken();
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
			token = lexer.getNextToken();
			if (token.getType() == TokenType.LP) {
				parseArguments();
			}
			// assume "PrimaryIdent -> IDENT" when another token than '(' is
			// read
			break;
		default:
			throw new ParserException(token);
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
			token = lexer.getNextToken();
			if (token.getType() == TokenType.LP) {
				// new object
				token = lexer.getNextToken();
				if (token.getType() == TokenType.RP) {
					token = lexer.getNextToken();
					return;
				} else {
					throw new ParserException(token);
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
			token = lexer.getNextToken();
			if (token.getType() == TokenType.LSQUAREBRACKET) {
				// new array
				parseNewArrayExpressionHelp();
			} else {
				throw new ParserException(token);
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
			token = lexer.getNextToken();
			parseExpression();
			if (token.getType() == TokenType.RSQUAREBRACKET) {
				token = lexer.getNextToken();
				while (token.getType() == TokenType.LSQUAREBRACKET) {
					token = lexer.getNextToken();
					if (token.getType() == TokenType.RSQUAREBRACKET) {
						token = lexer.getNextToken();
					} else {
						throw new ParserException(token);
					}
				}
				return;
			} else {
				throw new ParserException(token);
			}
		} else {
			throw new ParserException(token);
		}
	}

	public class ParserException extends Exception {
		private final Token unexpectedToken;

		private ParserException(Token t) {
			unexpectedToken = t;
		}

		@Override
		public String toString() {
			return "Line: " + unexpectedToken.getPosition().getLine()
					+ ". Unexpected token '" + unexpectedToken.getTokenString()
					+ "' at character: "
					+ unexpectedToken.getPosition().getCharacter();
		}

		@Override
		public String getMessage() {
			return "Line: " + unexpectedToken.getPosition().getLine()
					+ ". Unexpected token '" + unexpectedToken.getTokenString()
					+ "' at character: "
					+ unexpectedToken.getPosition().getCharacter();
		}
	}

}
