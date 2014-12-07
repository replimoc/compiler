class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int[] a = new int[3];
		a[0]=0;
		a[1]=1;
		a[2]=9;
		int[] b = new int[3];
		b[0]=2;
		b[1]=5;
		b[2]=8;
		System.out.println(horner(a,3,0));
		System.out.println(horner(a,3,1));
		System.out.println(horner(a,3,2));
		System.out.println(horner(b,3,0));
		System.out.println(horner(b,3,1));
		System.out.println(horner(b,3,2));
		System.out.println(horner(b,0,2));
	}
	
	public int horner(int[] p, int l, int x) {
		int r = 0;
		while (l>0) {
			r=r*x+p[l=l-1];
		}
		return r;
	}
}