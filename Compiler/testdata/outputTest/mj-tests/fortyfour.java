class FortyFour {

	int[] arr;

	public static void main(String[] args) {
		FortyFour f44 = new FortyFour();
		f44.init();
		f44.set(42);
		System.out.println(f44.arr[42]);
	}

	public void init() {
		this.arr = new int[44];
	}

	public void set(int index) {
		arr[index] = 44;
	}
}
