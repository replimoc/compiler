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
		int[] c = merge(a,3,b,3);
		int i=0;
		while (i < 6) {
			System.out.println(c[i]);
			i=i+1;
		}
	}
	
	public int[] merge(int[] a, int la, int[] b, int lb) {
		int[] help = new int[la+lb];
		int i = 0;
		int ia = 0;
		int ib = 0;
		while (i < la+lb) {
			if (ib >= lb || ia < lb && a[ia] <= b[ib]) {
				help[i] = a[ia];
				ia = ia+1;
			} else {
				help[i] = b[ib];
				ib = ib+1;
			}
			i=i+1;
		}
		return help;
	}		
}