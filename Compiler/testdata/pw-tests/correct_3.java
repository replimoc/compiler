/*This should actually get parsed successfully, although it will never compile, of course.
  I just tried to put a lot of stuff in here.
  If I overlooked something or did something wrong, add it or correct it ;-)  
*/

class World {

	public int worldId;
	
}

class Hello {
	
	public World world;

    public Hello Hello() {
		this.world = new World();
		{}{}{}{}{}{}{}{}{}{}{}
	}
	
	public void print() {
		System.out.println(world.worldId = world.worldId + 1);
	}
}

class Main {
	public static void foo(String[] bar) {
		if ((G) = A == B && C != Y / Z < H) {
			WorldArray ws = new World[A = B*10 || !C][];
			while (this.T % 42) {
				{
					ws[(bar)] = new Hello().world;
					ws.print();
				}
			}
		}
	}
}
