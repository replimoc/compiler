
class Fibonacci {
	public int recursive(int x) {
		if (x <= 2)
			return 1;
		return recursive(x - 1) + recursive(x - 2);
	}
	
	public int loop(int x) {
		int f1 = 1;
		int f2 = 1;
		
		int[ i = 3; /* parse error: missing ']' */
		while (i <= x) {
			int temp f1; /* missing '=' */
			f1 = f1 + f2;
			f2 = temp;
			i += 1;
		}
		return f1;
	}
}; /* ';' not allowed */
