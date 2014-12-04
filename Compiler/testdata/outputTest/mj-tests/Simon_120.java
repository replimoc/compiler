class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int[] t12 = new int[3];
		t12[0]=0;
		t12[1]=2;
		t12[2]=1;
		int[] cr = new int[3];
		cr[0]=2;
		cr[1]=0;
		cr[2]=1;
		int[] id = new int[3];
		id[0]=0;
		id[1]=1;
		id[2]=2;
		print3(comp(id,cr,3));
		print3(comp(t12,t12,3));
		print3(comp(cr,cr,3));
		print3(comp(cr,comp(cr,cr,3),3));
	}
	
	public int[] comp(int[] a, int[] b, int l) {
		int[] c = new int [l];
		int i = 0;
		while (i < l) {
			c[i] = a[b[i]];
			i=i+1;
		}
		return c;
	}
	
	public void print3(int[] q) {
		System.out.println(q[0]);
		System.out.println(q[1]);
		System.out.println(q[2]);
		System.out.println(-1);
	}		
}