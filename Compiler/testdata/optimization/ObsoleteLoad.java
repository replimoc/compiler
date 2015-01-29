class Test {
	public int b;
	
	public static void main(String[] args) {
		Test t = new Test();
		int[] a = new int[1];
		
		method(a[0], t.b);
		
		System.out.println(2);
	}
	
	public static void method(int i, int j) {
	}
}