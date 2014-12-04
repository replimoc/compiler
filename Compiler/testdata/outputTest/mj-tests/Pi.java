class Pi {
	public int getMaxInt() {
		int i = 0;
		while (i >= 0) {
			i = i + 1;
		}
		return i - 1;
	}

	public static void main(String[] args) {
		Pi pi = new Pi();

		int scale = 400000000;
		int i = 3;
		boolean plus = false;
		int currVal = scale;
		int maxInt = pi.getMaxInt();
		System.out.println(maxInt);
		while (i < maxInt) {
			if (plus) {
				currVal = currVal + scale / i;
			} else {
				currVal = currVal - scale / i;
			}

			/* System.out.println(currVal); uncomment and see the convergence */
			i = i + 2;
			plus = !plus;
		}
		System.out.println(currVal);
	}
}
