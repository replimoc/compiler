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

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public void parse() throws IOException, ParserException {
		token = lexer.getNextToken();
		parseProgram();
	}

	private void parseProgram() throws IOException, ParserException {
		while (token.getType() == TokenType.CLASS) {
			parseClassDeclaration();
		}

		if (token.getType() == TokenType.EOF) {
			return;
		} else {
			// throw new ParserException(t);
		}
	}

	private void parseClassDeclaration() throws IOException, ParserException {
		switch (token.getType()) {
		case CLASS:
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
				break;
			} else {
				parseClassMember();
				break;
			}
		default:
			throw new ParserException(token);
		}
	}

	private void parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			token = lexer.getNextToken();
			// Type
			if (token.getType() == TokenType.INT || token.getType() == TokenType.BOOLEAN || token.getType() == TokenType.VOID
					|| token.getType() == TokenType.IDENTIFIER) {
				token = lexer.getNextToken();
				// IDENT
				if (token.getType() == TokenType.IDENTIFIER) {
					token = lexer.getNextToken();
					if (token.getType() == TokenType.SEMICOLON) {
						// accept
						token = lexer.getNextToken();
						break;
					} else if (token.getType() == TokenType.LP) {
						token = lexer.getNextToken();

						if (token.getType() == TokenType.RP) {
							token = lexer.getNextToken();
							break;
						} else {
							parseParameters();
							if (token.getType() != TokenType.RP) {
								throw new ParserException(token);
							}
						}
						parseBlock();
						break;
					}
				}
				// static
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
				if (token.getType() != TokenType.IDENTIFIER || token.getSymbol().getValue().equals("String") == false) {
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

	private void parseField() {
		// check grammar
	}

	private void parseMethod() {
		// check grammar
	}

	private void parseMainMethod() {
		// check grammar
	}

	private void parseParameters() throws IOException, ParserException {
		parseParameter();
		if (token.getType() == TokenType.COMMA) {
			token = lexer.getNextToken();
			parseParameters();
		}
		// else accept
	}

	private void parseParameter() throws ParserException, IOException {
		parseType();
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token);
		}
		token = lexer.getNextToken();
	}

	private void parseType() throws IOException, ParserException {
		parseBasicType();
		while (token.getType() == TokenType.LSQUAREBRACKET) {
			token = lexer.getNextToken();
			if (token.getType() != TokenType.RSQUAREBRACKET) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();
		}
	}

	private void parseBasicType() throws IOException, ParserException {
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
	}

	private void parseStatement() {

	}

	private void parseBlock() {

	}

	private void parseBlockStatement() {

	}

	private void parseLocalVariableDeclarationStatement() {

	}

	private void parseEmptyStatement() {

	}

	private void parseWhileStatement() {

	}

	private void parseIfStatement() {

	}

	private void parseExpressionStatement() {

	}

	private void parseReturnStatement() {

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
			while (token.getType() == TokenType.LSQUAREBRACKET || token.getType() == TokenType.POINT) {
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
	private void parsePostfixOpMethodField() throws IOException, ParserException {
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
	private void parsePrimaryExpressionIdent() throws IOException, ParserException {
		switch (token.getType()) {
		case IDENTIFIER:
			token = lexer.getNextToken();
			if (token.getType() == TokenType.LP) {
				parseArguments();
			} else {
				throw new ParserException(token);
			}
			break;
		default:
			// epsilon
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
	private void parseNewArrayExpressionHelp() throws IOException, ParserException {
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
	}

}
