class a {
	public int x;
	public static void main(String[] args) {
		a q = new a();
		q.x = 2;
		q.x = 3*q.x;
		System.out.println(q.x);
	}
}
