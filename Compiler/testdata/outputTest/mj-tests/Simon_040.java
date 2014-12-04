class a {
	public int x;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int y = s(s(s(s(s(0)))));
		System.out.println(y);
	}
	
	public int s(int p) {
		System.out.println(p);
		System.out.println(x);
		x = x+1;
		return p+x;
	}
}
