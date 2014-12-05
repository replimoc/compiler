class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(pow(0,0));
		System.out.println(pow(2,0));
		System.out.println(pow(2,1));
		System.out.println(pow(2,8));
		System.out.println(pow(3,4));
		System.out.println(pow(7,3));
		System.out.println((pow(6,3)-pow(2,4))/(pow(7,3)-pow(3,5)));
	}
	
	public int pow(int b, int e) {
		int r = 1;
		while (e > 0) {
			e = e-1;
			r = r*b;
		}
		return r;
	}
}