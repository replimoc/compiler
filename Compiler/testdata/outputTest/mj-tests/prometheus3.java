/*prints 42 zeros and ones (alternating)*/
class HelloWorld {
	public static void main(String[] arg) {

		B test = new B();
		D check = new D();
		C ret = test.C();
		if (ret != null) {
			System.out.println(0);
			return;
		}
		test.x = 42;
		ret = test.C();
		boolean[][] result = check.filler(ret, test.x);

		int j = 0;

		while (j < test.x) {
			if (result[0][j]) {
				System.out.println(1);
			} else {
				System.out.println(0);
			}
			j = j + 1;
		}
	}

}

class B {
	public int x;

	public C C() {
		if (x == 42) {
			C C = new C();
			C.array = new boolean[x];
			int i = 0;
			while (i < x) {
				C.array[i] = i % 2 == 0;
				i = i + 1;
			}
			return C;
		} else {
			return null;
		}
	}
}

class C {
	public boolean[] array;

}

class D {
	public boolean[][] filler(C C, int size) {

		int j = 0;
		boolean[][] result = new boolean[1][];
		result[0] = new boolean[size];
		while (j < size) {
			result[0][j] = C.array[j];
			j = j + 1;
		}
		return result;
	}
}