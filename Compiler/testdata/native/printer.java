class StringUtils {
	public static native String create();
	public static native void printStatic(String test);

	public static void main(String[] args) {
		String string = StringUtils.create();
		StringUtils.printStatic(string);
		string.print();
	}
}

class String {
	public native void print();
}