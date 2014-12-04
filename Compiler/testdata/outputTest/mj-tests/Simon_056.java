class a {
	public a[] array;
	public int n;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		array = new a[4];
		a v = new a();
		array[1] = v;
		array[0] = v;
		array[3] = array[1];
		v.n = 3;
		v = new a();
		array[2] = v;
		v.n = 5;
		System.out.println(array[0].n);
		System.out.println(array[1].n);
		System.out.println(array[2].n);
		System.out.println(array[3].n);
	}
}