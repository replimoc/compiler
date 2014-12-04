class a {
	public static void main(String[] args) {
		a q = new a();
		int s = q.d(6);
		System.out.println(s);
	}
	
	public int d(int y) {
		return d2(y)-1;
	}
	
	public int d2(int y) {
		return y-2;
	}
}
