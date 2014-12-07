class a {
	public int x;
	public static void main(String[] args) {
		a q = new a();
		q.x = 4;
		q = q.d();
		System.out.println(q.x);
	}
	
	public a d() {
		a q = new a();
		q.x=3;
		return q;
	}
}
