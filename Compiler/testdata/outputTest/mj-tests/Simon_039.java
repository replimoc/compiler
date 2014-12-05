class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int y = s(s(s(s(s(0)))));
		System.out.println(y);
	}
	
	public int s(int p) {
		System.out.println(p);
		return p+1;
	}
}
