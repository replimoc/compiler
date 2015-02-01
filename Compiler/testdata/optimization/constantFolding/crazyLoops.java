class a {
	public static void main(String[] args) {
		int i = 0;
		while (i < 11) {
			int iBackup = i;
			i = i + 1;
			int l = 0;
			int h = i;
			int z = 0;
			while (z < 6) {
				int m = (h+l)/2;
				if (m*m > iBackup)
					h=m;
				else
					l=m;
				z = z + 1;
			}
			System.out.println(l);
		}
	}
}
