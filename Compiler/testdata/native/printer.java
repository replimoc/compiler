class Pointer {
	public native Pointer create();
	public native void print(Pointer test);

	public static void main(String[] args) {
		Pointer pointer = new Pointer();
		Pointer pointer2 = pointer.create();
		pointer.print(pointer2);
	}
}
