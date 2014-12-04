class a {
	public int n;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		n = 5;
		{
			int n;
			n=2;
			System.out.println(n);
		}
		System.out.println(n);
	}
}
