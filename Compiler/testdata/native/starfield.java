class Sdl {
	/** waits the specified number of milliseconds */
	public native void delay(int milliseconds);
	/** creates a new screen surface (SDL supports only 1 screen surface) */
	public native Surface setup_screen(int width, int height);
}

class Surface {
	/** fills surface with black. */
	public native void clear();
	/** must be called before drawing stuff (with put_pixel) */
	public native void begin_drawing();
	/** draws a pixel.
	 * x/y should be positive and smaller than width/height 
	 * Color components red, green, blue should be positive and
	 * smaller than 256. */
	public native void put_pixel(int x, int y, int red, int green, int blue);
	/** must be called after drawing before flipping a surface */
	public native void end_drawing();
	/** in case of the screen surface, flip double buffer */
	public native void flip();
}

class Star {
	public int pos_x;
	public int pos_y;
	public int speed_x;
	public int speed_y;

	public void draw(Surface surface) {
		surface.put_pixel(pos_x, pos_y, 255, 255, 255);
	}

	public void simulate() {
		pos_x = pos_x + speed_x;
		pos_y = pos_y + speed_y;
		if (pos_x >= 800 || pos_x < 0 || pos_y >= 600 || pos_y < 0) {
			pos_x = 400;
			pos_y = 300;
		}
	}
}

class Starfield {
	public int random_seed;

	public int get_random() {
		random_seed = 1664525 * random_seed + 1013904223;
		return random_seed;
	}

	public int get_random_max(int max) {
		int x = get_random();
		while (x < 0) {
			x = get_random();
		}
		return x % max;
	}

	public void run() {
		int width = 800;
		int height = 600;

		/* initialize stars */
		int n_stars = 400;
		Star[] stars = new Star[n_stars];
		int s = 0;
		while (s < n_stars) {
			Star star = new Star();
			star.speed_x = get_random_max(20) - 10;
			star.speed_y = get_random_max(20) - 10;
			star.pos_x = width / 2 + star.speed_x * get_random_max(50);
			star.pos_y = height / 2 + star.speed_y * get_random_max(50);
			star.simulate();

			stars[s] = star;
			s = s + 1;
		}

		/* initialize display */
		Sdl sdl = new Sdl();
		Surface screen = sdl.setup_screen(width, height);

		/* simulation/drawing loop */
		int steps = 10000;
		while (steps > 0) {
			screen.begin_drawing();
			screen.clear();
			s = 0;
			while (s < n_stars) {
				Star star = stars[s];
				s = s + 1;

				star.draw(screen);
				star.simulate();
			}
			screen.end_drawing();
			screen.flip();
			/* limit speed to ~20fps */
			sdl.delay(50);
			steps = steps-1;
		}
	}

	public static void main(String[] args) {
		new Starfield().run();
	}
}
