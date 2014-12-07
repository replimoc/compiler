class a {
	public int x;
	public static void main(String[] args) {
		a q = new a();
		q.d(q);
		System.out.println(q.x);
	}
	
	public void d(a q) {
		System.out.println(q.x);
		q.x = 1;
	}
}
