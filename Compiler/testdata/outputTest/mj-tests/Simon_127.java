class a {
	public static void main(String[] args) {
		new a().q();
	}
	
	public void q() {
		System.out.println(nCr(0,0));
		System.out.println(nCr(1,0));
		System.out.println(nCr(1,1));
		System.out.println(nCr(2,0));
		System.out.println(nCr(2,1));
		System.out.println(nCr(2,2));
		System.out.println(nCr(3,0));
		System.out.println(nCr(3,1));
		System.out.println(nCr(3,2));
		System.out.println(nCr(3,3));
		System.out.println(nCr(4,2));
	}
	
	public int nCr(int n, int k) {
		int[][] table = new int[n+1][];
		int i=0;
		while (i<=n) {
			table[i] = new int[i+1];
			int j=0;
			while ((j<=i) && (j <= k)) {
				if ((i == j) || (i == 0) || (j == 0)) {
					table[i][j] = 1;
				} else {
					table[i][j] = table[i-1][j-1]+table[i-1][j];
				}
				j=j+1;
			}
			i=i+1;
		}
		return table[n][k];
	}
}