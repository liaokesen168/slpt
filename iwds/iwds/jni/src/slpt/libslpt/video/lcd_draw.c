#include <common.h>
#include <fb_struct.h>

static unsigned int *base;
static int xres, yres, pixels_per_line;

void set_lcd_mem_base(void *p, int x, int y, int w) {
	base = p;
	xres = x;
	yres = y;
	pixels_per_line = w;
}

void fbs_set_lcd_mem_base(struct fb_struct *fbs) {
	base = fbs->base;
	xres = fbs->xres;
	yres = fbs->yres;
	pixels_per_line = fbs->pixels_per_line;
}

static inline void _put_pixel(int x, int y, int color)
{
	if (x < xres  && y < yres)
		base[y * pixels_per_line + x] = color;
}

void lcd_draw_clear(void) {
	if (base)
		memset(base, 0, pixels_per_line * yres * 4);
}

void draw_buf_64_32(unsigned int x, unsigned int y, unsigned char *p)
{
	int i, k;

	for (i=0; i<64; i++) {
		for (k=0; k<32; k++) {
//			_put_pixel(k, i, 0xffffffff);
			unsigned int n , off;
			off = i * 32 + k;
			n = off >> 3;
			off = off % 8;

			if (p[n] & 1 << (7 - off)) {
				_put_pixel(k + x, i + y, 0xffffffff);
			} else {
				_put_pixel(k + x, i + y, 0);
			}
		}
	}
}

void draw_buf_32_16(unsigned int x, unsigned int y, unsigned char *p)
{
	int i, k;

	for (i=0; i<32; i++) {
		for (k=0; k<16; k++) {
//			_put_pixel(k, i, 0xffffffff);
			unsigned int n , off;
			off = i * 16 + k;
			n = off >> 3;
			off = off % 8;

			if (p[n] & (1 << (7 - off))) {
				_put_pixel(k + x, i + y, 0xffffffff);
			} else {
				_put_pixel(k + x, i + y, 0);
			}
		}
	}
}

extern unsigned char num_font_64_32[][256];
void draw_num_64_32(unsigned int x, unsigned int y, unsigned int num)
{
	unsigned char *p = (unsigned char *)&num_font_64_32[num][0];

	draw_buf_64_32(x, y, p);
}

extern unsigned char num_font_32_16[][64];
void draw_num_32_16(unsigned int x, unsigned int y, unsigned int num)
{
	unsigned char *p = (unsigned char *)&num_font_32_16[num][0];

	draw_buf_32_16(x, y, p);
}

void draw_nums(unsigned int x, unsigned int y, unsigned int num)
{
	unsigned int nums[10];  // 429 496 7295
	unsigned int tmp = 1, i;
	volatile unsigned int flag0 = 1;

	memset(nums, 0, sizeof(int) * 10);

	if (num == 0) {
		draw_num_32_16(x, y, 0);
		return;
	}

	for (i = 0; i<10; i++) {
		nums[i] = (num % (10 * tmp)) / tmp ;
		tmp *= 10;
	}

	for (i = 0; i<10; i++) {
		if ((nums[9-i] == 0) && flag0) {
			// xxx
		} else {
			flag0 = 0;
			draw_num_32_16(x, y, nums[9-i]);
			x += 16;
		}
	}
}

extern unsigned char maoh[2][256];
void draw_maoh_64_32(unsigned int x, unsigned int y, unsigned int flag)
{
	unsigned char *p = (unsigned char *)&maoh[flag?1:0][0];

	draw_buf_64_32(x, y, p);
}


#define INT int
#define UINT unsigned int
void lcd_one_line(INT x1, INT y1, INT x2, INT y2, UINT color)
{
	INT dx, dy, e;
	dx = x2 - x1;
	dy = y2 - y1;

	if(dx >= 0){
		if(dy >= 0){
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1 <= x2){
					_put_pixel(x1, y1, color);
					if(e > 0){
						y1 += 1;
						e -= dx;
					}
					x1 += 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 <= y2){
					_put_pixel(x1, y1, color);
					if(e>0){
						x1 += 1;
						e -= dy;
					}
					y1 += 1;
					e += dx;
				}
			}
		}else{
			dy =- dy;
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1<=x2){
					_put_pixel(x1, y1, color);
					if(e > 0){
						y1 -= 1;
						e -= dx;
					}
					x1 += 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 >= y2){
					_put_pixel(x1, y1, color);
					if(e>0){
						x1 += 1;
						e -= dy;
					}
					y1 -= 1;
					e += dx;
				}
			}
		}
	}else{
		dx = -dx;
		if(dy >= 0){
			if(dx>=dy){
				e = dy - dx / 2;
				while(x1 >= x2){
					_put_pixel(x1, y1, color);
					if(e>0){
						y1 += 1;
						e -= dx;
					}
					x1 -= 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 <= y2){
					_put_pixel(x1, y1, color);
					if(e > 0){
						x1 -= 1;
						e -= dy;
					}
					y1 += 1;
					e += dx;
				}
			}
		}else{
			dy =- dy;
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1>=x2){
					_put_pixel(x1, y1, color);
					if(e>0){
						y1 -= 1;
						e -= dx;
					}
					x1 -= 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 >= y2){
					_put_pixel(x1, y1, color);
					if(e > 0){
						x1 -= 1;
						e -= dy;
					}
					y1 -= 1;
					e += dx;
				}
			}
		}
	}
}

/*
 * swap - swap value of @a and @b
 */

static inline void swap(int *x, int *y)
{
	int z = *x;
	*x = *y;
	*y = z;
}

void lcd_draw_line(INT x1, INT y1, INT x2, INT y2, INT thickness, UINT color)
{
	INT dx, dy, e, i;
	dx = x2 - x1;
	dy = y2 - y1;

	if(dx >= 0){
		if(dy >= 0){
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1 <= x2){
					for (i=0; i < thickness; i++)
						_put_pixel(x1, (y1 -( thickness >> 1)) + i, color);
					if(e > 0){
						y1 += 1;
						e -= dx;
					}
					x1 += 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 <= y2){
					for (i=0; i < thickness; i++)
						_put_pixel((x1 -( thickness >> 1)) + i, y1, color);
					if(e>0){
						x1 += 1;
						e -= dy;
					}
					y1 += 1;
					e += dx;
				}
			}
		}else{
			dy =- dy;
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1<=x2){
					for (i=0; i < thickness; i++)
						_put_pixel(x1, (y1 -( thickness >> 1)) + i, color);
					if(e > 0){
						y1 -= 1;
						e -= dx;
					}
					x1 += 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 >= y2){
					for (i=0; i < thickness; i++)
						_put_pixel((x1 -( thickness >> 1)) + i, y1, color);
					if(e>0){
						x1 += 1;
						e -= dy;
					}
					y1 -= 1;
					e += dx;
				}
			}
		}
	}else{
		dx = -dx;
		if(dy >= 0){
			if(dx>=dy){
				e = dy - dx / 2;
				while(x1 >= x2){
					for (i=0; i < thickness; i++)
						_put_pixel(x1, (y1 -( thickness >> 1)) + i, color);
					if(e>0){
						y1 += 1;
						e -= dx;
					}
					x1 -= 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 <= y2){
					for (i=0; i < thickness; i++)
						_put_pixel((x1 -( thickness >> 1)) + i, y1, color);
					if(e > 0){
						x1 -= 1;
						e -= dy;
					}
					y1 += 1;
					e += dx;
				}
			}
		}else{
			dy =- dy;
			if(dx >= dy){
				e = dy - dx / 2;
				while(x1>=x2){
					for (i=0; i < thickness; i++)
						_put_pixel(x1, (y1 -( thickness >> 1)) + i, color);
					if(e>0){
						y1 -= 1;
						e -= dx;
					}
					x1 -= 1;
					e += dy;
				}
			}else{
				e = dx - dy / 2;
				while(y1 >= y2){
					for (i=0; i < thickness; i++)
						_put_pixel((x1 -( thickness >> 1)) + i, y1, color);
					if(e > 0){
						x1 -= 1;
						e -= dy;
					}
					y1 -= 1;
					e += dx;
				}
			}
		}
	}
}
