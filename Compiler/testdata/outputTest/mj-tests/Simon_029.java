class a {
	public int x;
	public static void main(String[] args) {
		a q = new a();
		q.x = 2;
		System.out.println((q=new a()).x);
	}
}
