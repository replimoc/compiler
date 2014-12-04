class a {
	public a n;
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		a x = new a();
		a y = null;
		a[] z = new a[3];
		print(x == y);
		print(y == y);
		print(n == y);
		print(z[0] != null);
		print(id(y) == null);
	}
	
	public a id(a p) {
		return p;
	}
	
	public void print(boolean b) {
		if (b) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}
	}
}