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
		if (e==0) return 1;
		if (e % 2 == 0) return pow(b*b, e/2);
		return b*pow(b*b, e/2);
	}
}