class array {

	public int[][][][][][][][][][][][][] arr;

	public int[][] add(int[][] num1, int[][] num2) {
		int[][] temp = new int[num1.length][num1[0].length];
		for (int i = 0; i < temp.length; i++) {
			for (int j = 0; j < temp[i].length; j++) {
				temp[i][j] = num1[i][j] + num2[i][j];
			}
		}
		return temp;
	}

	public static void main(String[] args) {

		int[][] num1 = { { 1, 2 }, { 1, 2 }, { 1, 2 }, { 1, 2 } };
		int[][] num2 = new int[][] { { 3, 4 }, { 5, 6 }, { 7, 8 }, { 9, 10 } };

		int[][] num3 = add(num1, num2);
	}
}
