class a {
	public int x;
	public static void main(String[] args) {
		a q = new a();
		q.run();
	}
	
	public void run() {
		int s = d(3) + d(6);
		System.out.println(s);
	}
	
	public int d(int w) {
		x = x+w;
		System.out.println(x);
		return x;
	}
}
