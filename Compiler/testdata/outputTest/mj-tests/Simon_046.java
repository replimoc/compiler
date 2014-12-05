class a {
	public int x;
	public a next;
	public static void main(String[] a) {
		new a().q();
	}
	
	public void q() {
		a w = next().next().next();
		System.out.println(w.x);
	}
	
	public a next() {
		next = new a();
		next.x = x+1;
		return next;
	}
}
