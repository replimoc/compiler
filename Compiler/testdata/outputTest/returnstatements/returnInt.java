class Test {
	
	public static void main(String[] args) {
		Test t = new Test();

		System.out.println(t.method(3));
		System.out.println(t.methodSquare(3));
		System.out.println(t.methodDouble(3));
	}
	
	public int method(int x) {
		return x;
	}
	
	public int methodSquare(int x) {
		return x*x;
	}
	
	public int methodDouble(int x) {
		return x+x;
	}
}