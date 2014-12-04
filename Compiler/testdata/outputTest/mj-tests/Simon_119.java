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
		sort(a, 6);
		int i = 0;
		while (i < 6) {
			System.out.println(a[i]);
			i=i+1;
		}
	}
	
	public void sort(int[] a, int l) {
		int i = 0;
		while (i < l) {
			int j = i+1;
			int index = i;
			int min = a[i];
			while (j<l) {
				if (a[j] < min) {
					index=j;
					min = a[j];
				}
				j=j+1;
			}
			int temp = a[index];
			a[index] = a[i];
			a[i] = temp;
			i=i+1;
		}
	}		
}