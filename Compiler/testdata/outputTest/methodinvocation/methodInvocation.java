class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.print1();
		t.print2();
		t.print3();
		t.returnThis().returnThis().returnThis().print1();
	}
	
	public void print1() {
		System.out.println(1);
	}
	
	public void print2() {
		System.out.println(2);
	}
	
	public void print3() {
		System.out.println(3);
	}
	
	public Test returnThis() {
		return this;
	}
}