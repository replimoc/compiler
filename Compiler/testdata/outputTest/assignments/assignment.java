class Test {
	public static void main(String[] args) {
		int x = 10;
		int y;
		int z;
		y = x;
		z = x + y;
		
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
		
		boolean a = false;
		boolean b = true;
		boolean c = a || b;
		
		if(c) {
			System.out.println(20);
		} else {
			System.out.println(-20);
		}
		
		Test t = new Test();
		x = t.method();
		System.out.println(20);
		System.out.println(y);
	}
	
	public int method() {
		return 20;
	}
	
}