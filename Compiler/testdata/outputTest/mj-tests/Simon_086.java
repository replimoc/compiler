class a {
	public int x;
	public static void main(String[] args) {
		a w = new a();
		if (w.q()) {
			System.out.println(w.x);
		}
	}
	
	public boolean q() {
		x = x+1;
		return true;
	}
}