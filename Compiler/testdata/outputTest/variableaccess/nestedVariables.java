class Test {
	public int x;

	public static void main(String[] args) {
		this.x = 10;
		int x = 1;
		{
			int x = 2;
			{
				int x = 3;
				{
					int x = 4;
					{
						int x = 5;
						{
							int x = 6;
							{
								int x = 7;
								{
									int x = 8;
									System.out.println(x);
									System.out.println(this.x);
								}
								System.out.println(x);
								System.out.println(this.x);
							}
							System.out.println(x);
							System.out.println(this.x);
						}
						System.out.println(x);
						System.out.println(this.x);
					}
					System.out.println(x);
					System.out.println(this.x);
				}
				System.out.println(x);
				System.out.println(this.x);
			}
			System.out.println(x);
			System.out.println(this.x);
		}
		System.out.println(x);
		System.out.println(this.x);
	}
}