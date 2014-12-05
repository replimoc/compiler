class A {
	public int x;
	public boolean y;
	
	public static void main(String[] args) {
		System.out.println(5);
		
		A a = new A();
		a.x = 4;
		a.y = false;
		System.out.println(a.x);
		
		a.method1();
		System.out.println(a.method2());
		
		a.method3();
		
		a.print(1);
		a.print2(false);
		
		int b = a.x + 42;
		boolean c = a.y; 
	}
	
	public void method1() {
		System.out.println(x - 1);
	}
	
	public int method2() {
		this.x = 2;
		this.x = this.x + 2;
		boolean var = this.y; 
		return x - 2;
	}
	
	public boolean method3() {
		x = 2;
		x = x + 1;
		
		if(y == false) {
			return true;
		} else {
			return false;
		}
	}
	
	public void print(int value) {
		System.out.println(value);
	}
	
	public void print2(boolean value) {
		if(value == true) {
			System.out.println(42);
		} else {
			System.out.println(0);
		}
	}
}