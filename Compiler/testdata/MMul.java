class Matrix {
	public int[] data;
	public int rows;
	public int cols;

	public void init(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		data = new int[rows * cols];
	}

	public int get(int i, int j) {
		return data[i * cols + j];
	}

	public void set(int i, int j, int val) {
		data[i * cols + j] = val;
	}

	public void print() {
		int i = 0;
		while (i < rows) {
			int j = 0;
			while (j < cols) {
				System.out.println(get(i, j));
				j = j + 1;
			}
			i = i + 1;
		}
	}

	public void fill() {
		int i = 0;
		while (i < rows) {
			int j = 0;
			while (j < cols) {
				set(i, j, i + j);
				j = j + 1;
			}
			i = i + 1;
		}
	}

	public int sum() {
		int sum = 0;
		int i = 0;
		while (i < rows) {
			int j = 0;
			while (j < cols) {
				sum = sum + get(i, j);
				j = j + 1;
			}
			i = i + 1;
		}
		return sum;
	}
}

class MMul {
	public Matrix mul(Matrix a, Matrix b) {
		if (a.cols != b.rows)
			return null;
		Matrix res = new Matrix();
		res.init(a.rows, b.cols);

		int i = 0;
		while (i < res.rows) {
			int k = 0;
			while (k < a.cols) {
				int j = 0;
				while (j < res.cols) {
					int newVal = res.get(i, j) + a.get(i, k) * b.get(k, j);
					res.set(i, j, newVal);
					j = j + 1;
				}
				k = k + 1;
			}
			i = i + 1;
		}

		return res;
	}

	public static void main(String[] args) {
		Matrix a = new Matrix();
		a.init(500, 1000);
		a.fill();
		Matrix b = new Matrix();
		b.init(1000, 500);
		b.fill();
		Matrix res = new MMul().mul(a, b);
		System.out.println(res.sum());
	}
}
