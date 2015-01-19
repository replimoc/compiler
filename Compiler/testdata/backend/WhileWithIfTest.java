class WhileTest {
	public static void main(String[] args) {

		int x = 0;
		int y = 4;

		while (x < 10)
		{
			y = 2 + x;
			if (x < 4) {
				if( x > 1) {
					y = 7;
				} else {
					y = 19;
				}
			} else {
				y = y + 1;
			}
			x = x + 1;
		}

		System.out.println(y);
	}
}