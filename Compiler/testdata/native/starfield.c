#include <SDL/SDL.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>

static SDL_Surface *screen;

void*  __attribute__((sysv_abi))   Sdl$setup_screen(void *_this, int width, int height)
{
	if (SDL_Init(SDL_INIT_EVERYTHING) < 0) {
		fprintf(stderr, "SDL_Init failed\n");
		exit(1);
	}
	screen = SDL_SetVideoMode(width, height, 24, SDL_SWSURFACE | SDL_DOUBLEBUF);
	if (screen == NULL) {
		fprintf(stderr, "SDL_SetVideoMode failed\n");
		exit(2);
	}
	/* prevent SDL from grabbing SIGINT (CTRL+C) */
	signal(SIGINT, SIG_DFL);

	atexit(SDL_Quit);

	return screen;
}

void  __attribute__((sysv_abi))  Sdl$delay(void *_this, int ms)
{
	SDL_Delay(ms);
}

void  __attribute__((sysv_abi))  Surface$begin_drawing(SDL_Surface *surface)
{
	SDL_LockSurface(surface);
}

void  __attribute__((sysv_abi))  Surface$clear(SDL_Surface *surface)
{
	SDL_FillRect(surface, NULL, 0);
}

void  __attribute__((sysv_abi))  Surface$put_pixel(SDL_Surface *surface, int x, int y, int r, int g, int b)
{
	/* inefficient but easy to use interface :) */
	Uint32 pixel = SDL_MapRGB(surface->format, r, g, b);
	int bpp = surface->format->BytesPerPixel;
	char *p = (char*)surface->pixels + y * surface->pitch + x * bpp;
	switch (bpp) {
	case 3:
		if (SDL_BYTEORDER == SDL_BIG_ENDIAN) {
			p[0] = (pixel >> 16) & 0xff;
			p[1] = (pixel >> 8) & 0xff;
			p[2] = pixel & 0xff;
		} else {
			p[0] = pixel & 0xff;
			p[1] = (pixel >> 8) & 0xff;
			p[2] = (pixel >> 16) & 0xff;
		}
		break;

	case 4:
		*(Uint32 *)p = pixel;
		break;

	default:
		fprintf(stderr, "Unsupported BPP in sdl_put_pixel");
	}
}

void  __attribute__((sysv_abi))  Surface$end_drawing(SDL_Surface *surface)
{
	SDL_UnlockSurface(surface);
}

void  __attribute__((sysv_abi))  Surface$flip(SDL_Surface *surface)
{
	SDL_Flip(surface);
}
