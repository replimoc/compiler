class Test {
	public int i;
	
	public static void main(String[] args) {
		Test t = new Test();
		t.method();
	}
	
	public void method() {
		i = 0;
		
		while(i < 4) {
			System.out.println(i);
			i = i + 2;
		}
	}
}