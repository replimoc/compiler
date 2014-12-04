class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		int[] a = new int[6];
		a[0]=2;
		a[1]=8;
		a[2]=0;
		a[3]=7;
		a[4]=5;
		a[5]=1;
		System.out.println(hindex(a,6));
		a[0]=2;
		a[1]=2;
		System.out.println(hindex(a,2));
	}
	
	public int hindex(int[] a, int l) {
		int[] help = new int[l+1];
		int i = 0;
		while (i < l) {
			int n = a[i];
			if (n > l) n=l;
			help[n]=help[n]+1;
			i=i+1;
		}
		i=l;
		while (i > 0) {
			help[i-1]=help[i-1]+help[i];
			i=i-1;
		}
		i=0;
		while (i <= l) {
			if (i>help[i]) return i-1;
			i=i+1;
		}
		return l;
	}		
}