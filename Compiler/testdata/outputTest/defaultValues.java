class Test {
	public int i;
	public boolean b;
	public Test t;
	public int[] ia;
	public boolean[] ba;
	public Test[] ta;

	public static void main(String[] args) {
		Test t = new Test();
		t.ia = new int[5];
		t.ba = new boolean[5];
		t.ta = new Test[5];

		System.out.println(t.i);
		t.printBoolean(t.b);
		t.printTest(t.t);

		int i = 0;
		while (i < 5) {
			System.out.println(i + 10);

			System.out.println(t.ia[i]);
			t.printBoolean(t.ba[i]);
			t.printTest(t.ta[i]);

			i = i + 1;
		}
	}

	public void printBoolean(boolean b) {
		if (b) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}

	public void printTest(Test t) {
		if (t != null) {
			System.out.println(1);
		} else {
			System.out.println(0);
		}
	}
}
