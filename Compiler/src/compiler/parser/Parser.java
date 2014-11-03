package compiler.parser;

import java.io.IOException;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;

public class Parser {
	private final Lexer lexer;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public void parse() throws IOException {
		parseProgram();
	}

	private void parseProgram() throws IOException {
		Token t = lexer.getNextToken();
		while (t.getType() == TokenType.CLASS) {
			parseClassDeclaration();
		}

		if (t.getType() == TokenType.EOF) {
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

	private void parseUnaryExpression() {

	}

	private void parsePostfixExpression() {

	}

	private void parsePostfixOp() {

	}

	private void parseMethodInvocation() {

	}

	private void parseArrayAccess() {

	}

	private void parseFieldAccess() {

	}

	private void parseArguments() {

	}

	private void parsePrimaryExpression() {

	}

	private void parseNewObjectExpression() {

	}

	private void parseNewArrayExpression() {

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
