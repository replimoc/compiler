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

	public void parse() throws IOException {
		token = lexer.getNextToken();
		parseProgram();
	}

	private void parseProgram() throws IOException {
		while (token.getType() == TokenType.CLASS) {
			parseClassDeclaration();
		}

		if (token.getType() == TokenType.EOF) {
			return;
		} else {
			// throw new ParserException(t);
		}
	}

	private void parseClassDeclaration() {

	}

	private void parseClassMember() {
		// check grammar
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

	private void parseParameters() {
		// check grammar
	}

	private void parseParameter() {
		// check grammar
	}

	private void parseType() {
		// remove left recursion
	}

	private void parseBasicType() {

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
	 */
	private void parseUnaryExpression() throws IOException {
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
			// throw new ParserException(token);
		}

	}

	/**
	 * PostfixExpression -> PrimaryExpression (PostfixOp)*
	 * 
	 * @throws IOException
	 */
	private void parsePostfixExpression() throws IOException {
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
			// throw new ParserException(token);
		}
	}

	/**
	 * PostfixOp -> MethodInvocationFieldAccess | ArrayAccess
	 * 
	 * @throws IOException
	 */
	private void parsePostfixOp() throws IOException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			parseArrayAccess();
			break;
		case POINT:
			parsePostfixOpMethodField();
			break;
		default:
			// throw new ParserException(token);
		}

	}

	/**
	 * MethodInvocationFieldAccess -> . IDENT MethodInvocationFieldAccess'
	 * 
	 * @throws IOException
	 */
	private void parsePostfixOpMethodField() throws IOException {
		if (token.getType() == TokenType.POINT) {
			token = lexer.getNextToken();
			if (token.getType() == TokenType.IDENTIFIER) {
				token = lexer.getNextToken();
				parseMethodInvocation();
			} else {
				// throw new ParserException(token);
			}
		} else {
			// throw new ParserException(token);
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
	 */
	private void parseArrayAccess() throws IOException {
		switch (token.getType()) {
		case LSQUAREBRACKET:
			token = lexer.getNextToken();
			parseExpression();
			if (token.getType() == TokenType.RSQUAREBRACKET) {
				token = lexer.getNextToken();
				return;
			} else {
				// throw new ParserException(token);
			}
			break;
		default:
			// throw new ParserException(token);
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
	 */
	private void parsePrimaryExpression() throws IOException {
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
			// throw new ParserException(token);
		}
	}

	/**
	 * PrimaryIdent -> IDENT | IDENT ( Arguments )
	 * 
	 * @throws IOException
	 */
	private void parsePrimaryExpressionIdent() throws IOException {
		switch (token.getType()) {
		case IDENTIFIER:
			token = lexer.getNextToken();
			if (token.getType() == TokenType.LP) {
				parseArguments();
			} else {
				// throw new ParserException(token);
			}
			break;
		default: // epsilon
		}
	}

	/**
	 * NewExpression -> IDENT () | BasicType NewArrayExpression
	 * 
	 * @throws IOException
	 */
	private void parseNewExpression() throws IOException {
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
					// throw new ParserException(token);
				}
			} else if (token.getType() == TokenType.LSQUAREBRACKET) {
				// new array
				parseNewArrayExpressionHelp();
			} else {
				// throw new ParserException(token);
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
				// throw new ParserException(token);
			}
			break;
		default:
			// throw new ParserException(token);
		}
	}

	/**
	 * NewArrayExpression -> [Expression] ([])*
	 * 
	 * @throws IOException
	 */
	private void parseNewArrayExpressionHelp() throws IOException {
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
						// throw new ParserException(token);
					}
				}
				return;
			} else {
				// throw new ParserException(token);
			}
		} else {
			// throw new ParserException(token);
		}
	}

	private class ParserException extends Exception {
		private final Token unexpectedToken;

		private ParserException(Token t) {
			unexpectedToken = t;
		}

		@Override
		public String toString() {
			return "Line: " + unexpectedToken.getPosition().getLine() + ". Unexpected token '" + unexpectedToken.getTokenString()
					+ "' at character: "
					+ unexpectedToken.getPosition().getCharacter();
		}
	}

}
