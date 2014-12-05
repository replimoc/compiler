class HelloWorld {

	public int y;

	public static void main(String[] args) {
		HelloWorld x = new HelloWorld();
		x.y = 332;

		int t = x.getArray()[1+1+1+1+1+1] = 4;

		int[] array = new int[t];

		array = x.getArray();

		if (array[1] == 1) {
			if (true) {
				System.out.println(0);
			}
		}

		boolean[] boolarr = new boolean[4];
		boolarr = x.getBooleanArr(true, false || true && false, 3 < 5);
		x.print(boolarr[0]);
		x.print(boolarr[1]);
		x.print(boolarr[2]);
	}

	public int[] getArray() {
		int[] res = new int[12];
		res[1] = 1;
		res[2] = 2;
		return res;
	}

	public boolean[] getBooleanArr(boolean a, boolean b, boolean c) {
		boolean[] res = new boolean[3];
		res[0] = a;
		res[1] = b;
		res[2] = c;
		return res;
	}

	public void print(boolean x) {
		if (x) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}

}