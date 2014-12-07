class a {
	public int x;
	public static void main(String[] args) {
		a w = new a();
		while (w.q() < 4) {
			System.out.println(w.x);
		}
	}
	
	public int q() {
		x = x+1;
		return x;
	}
}