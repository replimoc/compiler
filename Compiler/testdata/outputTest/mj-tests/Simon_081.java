class a {
	public static void main(String[] args) {
		new a().w();
	}
	
	public void w() {
		a x = new a();
		a y = new a();
		print(x == y);
		print(x == x);
		print(y == y);
		print(x != y);
		print(x != x);
		print(y != y);
		print(x == id(x));
		print(id(x) == y);
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