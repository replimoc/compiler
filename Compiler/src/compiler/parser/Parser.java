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

	private void parseClassDeclaration() throws IOException, ParserException {
		switch (token.getType()) {
		case CLASS:
			token = lexer.getNextToken();
			if (token.getType() != TokenType.IDENTIFIER) {
				throw new ParserException(token);
			}
			if (token.getType() != TokenType.LCURLYBRACKET) {
				throw new ParserException(token);
			}
			parseClassMember();
			if (token.getType() != TokenType.RCURLYBRACKET) {
				throw new ParserException(token);
			}
		default:
			throw new ParserException(token);
		}
	}

	private void parseClassMember() throws ParserException, IOException {
		switch (token.getType()) {
		case PUBLIC:
			token = lexer.getNextToken();
			if (token.getType() == TokenType.INT || token.getType() == TokenType.BOOLEAN || token.getType() == TokenType.VOID || token.getType() == TokenType.IDENTIFIER) {
				token = lexer.getNextToken();
				if (token.getType() == TokenType.IDENTIFIER) {
					token = lexer.getNextToken();
					if (token.getType() == TokenType.SEMICOLON) {
						// accept
					} else if (token.getType() == TokenType.LP) {
						token = lexer.getNextToken();
						parseParameters();
						token = lexer.getNextToken();
						if (token.getType() != TokenType.RP) {
							throw new ParserException(token);
						}
						token = lexer.getNextToken();
						parseBlock();
					} else {
						throw new ParserException(token);
					}
				} else {
					throw new ParserException(token);
				}
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
				if (token.getType() != TokenType.IDENTIFIER || token.getTokenString().equals("String") == false) {
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
			} else {
				throw new ParserException(token);
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
		token = lexer.getNextToken();
		if (token.getType() == TokenType.COMMA) {
			token = lexer.getNextToken();
			parseParameters();
		}
		// else accept
	}

	private void parseParameter() throws ParserException, IOException {
		parseType();
		token = lexer.getNextToken();
		if (token.getType() != TokenType.IDENTIFIER) {
			throw new ParserException(token);
		}
	}

	private void parseType() throws IOException, ParserException {
		parseBasicType();
		token = lexer.getNextToken();
		while (token.getType() == TokenType.LSQUAREBRACKET) {
			token = lexer.getNextToken();
			if (token.getType() != TokenType.RSQUAREBRACKET) {
				throw new ParserException(token);
			}
			token = lexer.getNextToken();
		}
	}

	private void parseBasicType() throws IOException, ParserException {
		switch(token.getType()) {
		case INT :
		case BOOLEAN :
		case VOID :
		case IDENTIFIER :
			token = lexer.getNextToken();
			break;
		default : 
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

	private void parseUnaryExpression() {

	}

	private void parsePostfixExpression() {

	}

	private void parsePostfixOp() {

	}

	private void parseMethodInvocation() {

	}

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

	private void parseFieldAccess() {

	}

	private void parseArguments() {

	}

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
			token = lexer.getNextToken();
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

	private void parsePrimaryExpressionIdent() {
		switch (token.getType()) {
		case LP:
			parseArguments();
			break;
		default: // epsilon
		}
	}

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

	private void parseNewArrayExpressionHelp() throws IOException {
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
	}

	private class ParserException extends Exception {
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
