class Sorter
{	
	public void initData(int[] data, int length) {
		int i = 0;
		data[0] = 0;
		while(i < length) {
			data[i] = length - i;
			i = i + 1;
		}
	}

	public void printData(int[] data, int length) {
		int i = 0;
		while(i < length) {
			System.out.println(data[i]);
			i = i + 1;
		}
	}

	public void sort(int[] data, int length) {
		int i = length; 
		while (i > 0) {
			int j = 0;
			while (j < length - 1 && j < i) {
				if (data[j] > data[j+1]) {
					swap(data, j, j+1);
				}
				j = j + 1;
			}
			i = i - 1;
		}
	}

	public void swap(int[] data, int i, int j) {
		int cpy = data[i];
		data[i] = data[j];
		data[j] = cpy;
	}

	public static void main(String[] args) {
		Sorter sorter = new Sorter();
		int length = 1000;
		int[] data = new int[length];
		sorter.initData(data, length);
		sorter.printData(data, length);
		sorter.sort(data, length);
		sorter.printData(data, length);
	}
}
