class Test {

	public static void main(String[] args) {
		boolean flag = true;
		
		if (flag = false == false == false == false) {
			System.out.println(1);
		} else {
			System.out.println(2);
		}

		if (flag = false == false == false) {
			System.out.println(3);
		} else {
			System.out.println(4);
		}

		if (flag = false == false) {
			System.out.println(5);
		} else {
			System.out.println(6);
		}

	}
}