class Test {
	public static void main(String[] args) {
		Test t = new Test();
		boolean flag = t.test(42);
		if(flag) {
			System.out.println(1);
		} else {
			System.out.println(2);
		}
		
		flag = t.test(17);
		if(flag) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
	
	public boolean test(int x) 
	{ 
		return x == 42; 
	}
}