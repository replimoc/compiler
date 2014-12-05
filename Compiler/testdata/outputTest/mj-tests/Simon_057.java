class a {
	public a[] array;
	public int n;
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		array = new a[4];
		n = 3;
		a v = new a();
		v.n = 4;
		v.array = new a[2];
		array[0] = v;
		v.array[1] = this;
		System.out.println(n);
		System.out.println(array[0].n);
		System.out.println(array[0].array[1].n);
		System.out.println(array[0].array[1].array[0].n);
		System.out.println(array[0].array[1].array[0].array[1].n);
	}
}