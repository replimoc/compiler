class A {
}

class Test {
	public static void main(String[] args) {
		A[] a = new A[5];
		A b = new A();
		a[10] = new A(); /* no error?: index out of bounds */
		b[10] = new A(); /* error: no array */
		a[b] = new A(); /* error: index not a number */
		a[false] = new A(); /* error: index not a number */
	}
}
