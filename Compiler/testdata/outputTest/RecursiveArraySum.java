class Sum {
	
	public void initData(int[] data, int length) {
		int i = 0;
		data[0] = 0;
		while(i < length) {
			data[i] = i;
			i = i + 1;
		}
	}
	
	public int sum(int[] data, int sInd, int eInd) {
		if (sInd + 1 == eInd) {
			return data[sInd] + data[eInd];
		} else if (sInd == eInd) {
			return data[sInd];
		}

		
		int half = sInd + (eInd - sInd) / 2;
		
		return sum(data, sInd, half) + sum(data, half + 1, eInd);
	}

	public static void main(String[] args) {
		Sum sum = new Sum();
		
		int size = 1000000;
		int[] data = new int[size];
		sum.initData(data, size);

		int arrSum = sum.sum(data, 0, size - 1);
		System.out.println(arrSum);

		int num = 0;
		int i = 0;
		while (i < size) {
			num = num + i;
			i = i + 1;
		}
		System.out.println(num);
	}
}
