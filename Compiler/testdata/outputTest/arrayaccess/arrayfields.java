class Arr
{
	public int[] numbers;
	public E[] elements;

	public static void main(String[] args) {
		Arr arr = new Arr();
		arr.numbers = new int[2];
		arr.numbers[0] = 10;
		System.out.println(arr.numbers[0]);

		arr.elements = new E[2];
		arr.elements[0] = new E();
		arr.elements[0].x = 5;
		System.out.println(arr.elements[0].x);

	}
}

class E
{
	public int x;
}