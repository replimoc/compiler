class a {
	public int x;
	public a s;
	public static void main(String[] args) {
		a q = new a();
		q.s = new a();
		q.s.s = new a();
		q.s.s.s = new a();
		q.s.x=1;
		q.s.s.x=3;
		q.s.s.s.x=5;
		System.out.println(q.s.x);
		System.out.println(q.s.s.x);
		System.out.println(q.s.s.s.x);
	}
}
