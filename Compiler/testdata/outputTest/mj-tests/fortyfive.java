class FortyFive {

	public int foo(int a) {
		if (a == 0 || a == 1)
			return a;
		return foo(a-1) + foo(a-2); 
	}

	public static void main(String[] args) {
		FortyFive fiver = new FortyFive();
		int res = fiver.foo(2);
		System.out.println(res + 44);
	}

}
