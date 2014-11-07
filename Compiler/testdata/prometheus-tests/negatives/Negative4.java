/**
 * Program that is correctly lexed but yields a Parser error
 * due to a number that starts with 0. The lexer recognizes
 * two INTEGER_LITERALS, '0' and '123456789'.
 */
class Programm1 {
	public int Funktion() {
		return 0123456789;
	}
}
