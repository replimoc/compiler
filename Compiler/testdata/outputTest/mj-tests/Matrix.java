class Matrix {
	
	public int[][] data;
	public int size;
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void create2DimArray() {
		data = new int[size][];
		int i = 0; 
		while (i < size) {
			data[i] = new int[size];
			i = i + 1;
		}
	}
	
	public void init() {
		int i = 0;
		while (i < size*size) {
			int fI = i / size;
			int sI = i % size;
			
			data[fI][sI] = i;
			
			i = i + 1;
		}
	}
	
	public void output() {
		int i = 0; 
		while (i < size) {
			int j = 0;
			while (j < size) {
				System.out.println(data[i][j]);
				j = j + 1;
			}
			i = i + 1;
		}
	}
	
	public void transpose() {
		int[][] original = data;
		create2DimArray();
		
		int i = 0;
		while (i < size*size) {
			int fI = i / size;
			int sI = i % size;
			
			data[fI][sI] = original[sI][fI];
			
			i = i + 1;
		}
	}
	
	public void printLastRow(int[][] data, int size) {
		int i = 0;
		while (i < size) {
			System.out.println(data[size - 1][i]);
			i = i + 1;
		}
	}
	
	public void printLastColumn(int[][] data, int size) {
		int i = 0;
		while (i < size) {
			System.out.println(data[i][size - 1]);
			i = i + 1;
		}
	}
	
	public boolean check(int[][] m1, int[][] m2, int size) {
		boolean fail = false;
		
		int i = 0;
		while (i < size*size) {
			int fI = i / size;
			int sI = i % size;
			
			fail = m1[fI][sI] != m2[fI][sI];
			
			i = i + 1;
		}
		
		if (fail && check(null, null, -42)) {
			int failNumber = 1 / 0;
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		Matrix matrix = new Matrix();
		matrix.setSize(10);
		matrix.create2DimArray();
		matrix.init();
		int[][] m1 = matrix.data;
		matrix.transpose();
		matrix.check(m1, matrix.data, matrix.size);
		matrix.printLastRow(m1, matrix.size);
		matrix.printLastColumn(matrix.data, matrix.size);
	}
}
