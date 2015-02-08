class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method(6, true);
	}
	
	public void method(int x, boolean y) {
		int z = x / 3;
		int a = x / 3;
		int b = x / 3;
		int c = x / 3;
		int d = x / 3;
		int e = x / 3;
		int f = x / 3;
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		System.out.println(e);
		System.out.println(f);
		
		if(y) {
			System.out.println(x / 3);
		} else {
			System.out.println(z);
		}
		
		int i = 0;
		while(i < 1) {
			System.out.println(x / 3);
			i = i + 1;
		}
	}
}