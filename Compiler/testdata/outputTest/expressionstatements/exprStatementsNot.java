class Test {
	
	public static void main(String[] args) {
    if (!true) {
      int i = 1 / 0;
    }
    if (!false) {
      System.out.println(42);
    }
		boolean flag = true;
    if (!flag) {
      int i = 1 / 0;
    }
    flag = false;
    if (!flag) {
      System.out.println(42);
    }
	}
}