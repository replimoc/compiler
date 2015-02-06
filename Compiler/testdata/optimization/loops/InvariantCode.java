class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.method(5, 2);
		t.method2(5, 2, true);
	}
	
	public void method(int x, int y) {
		int i = 0;
		while(i < y) {
			System.out.println(x * 20);
			i = i + 1;
		}
	}
	
	public void method2(int x, int y, boolean b) {
		int i = 0;
		while(i < y) {
			if(b) {
				System.out.println(x * 20);
			}
			i = i + 1;
		}
	}
}