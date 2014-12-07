class A {
	
	public int y;
	
	public static void main(String[] args) {
		A x = new A();
		x.y = 332;
		System.out.println(x.getA().getA().y);
		
		x = x.getA();
		int t = x.getA().y = 5;
		
		int[] array = new int[x.getIndex()];
	}
	
	public A getA(){
		A res = new A();
		res.y = 22;
		return res;
	}
	
	public int getIndex() {
		return 12;
	}
	
}