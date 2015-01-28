class Test {
	
	public static void main(String[] args) {
		Test t = new Test();
		System.out.println(t.method(1));
		System.out.println(t.method(2));
		System.out.println(t.method(3));
	}
	
	public int method(int x) {
		if(x == 1) {
			return x;
		} else {
			return x+x;
		}
	}
}