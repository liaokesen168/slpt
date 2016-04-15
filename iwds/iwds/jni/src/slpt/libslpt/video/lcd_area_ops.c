#include <string.h>
#include <fb_struct.h>

int global_view_reinit_flag;
unsigned int alpha32 = 0x00ffffff;

#undef min
#undef max

#define min(a, b)  ((a) < (b) ? (a) : (b))
#define max(a, b)  ((a) > (b) ? (a) : (b))

#define MEMCPY1(dst, src)                       \
    do {                                        \
        *((dst)++) = *((src)++);                \
    } while (0)

#define MEMCPY1_ALPHA(dst, src, alpha_color)    \
   do {                                         \
        if (*(src) != (alpha_color))            \
            *((dst)++) = *((src)++);            \
        else {                                  \
            (dst)++;                            \
            (src)++;                            \
        }                                       \
    } while (0)

#define MEMCPY1_ALPHA_CLEAR(dst, src, alpha_color, bg_color)\
   do {                                                     \
        if (*(src) != (alpha_color))                        \
            *((dst)++) = *((src)++);                        \
        else {                                              \
            *((dst)++) = bg_color;                          \
            (src)++;										\
        }                                                   \
    } while (0)

#define MEMCPY1_ALPHA_REPLACE(dst, src, bg_pic, alpha_color, replace_color)	\
   do {                                                     \
	   if (*((src)++) != (alpha_color)) {					\
		   *((dst)++) = replace_color;						\
		   (bg_pic)++;										\
       } else {												\
		   *((dst)++) = *((bg_pic)++);						\
        }                                                   \
    } while (0)


#define MEMCPY1_ALPHA_REPLACE_SRC(dst, src, bg_pic, alpha_color)	\
	do {															\
		if (*(src) != (alpha_color)) {								\
			*((dst)++) = *((src)++);								\
			(bg_pic)++;												\
		} else {													\
			(src)++;												\
			*((dst)++) = *((bg_pic)++);								\
		}															\
	} while (0)

#define lcd_copy_common(nwords, nlines, exp, exp_next_line)        \
do {                                                               \
  int mclines = (nlines);                                          \
  int mctmp = (nwords), mcn;                                       \
  if (mctmp < 8) mcn = 0; else { mcn = mctmp/8; mctmp %= 8; }      \
  int mci, mcj;                                                    \
  for (mci = 0; mci < mclines; mci++) {                            \
    switch (mctmp) {                                               \
      case 7:           exp;                                       \
      case 6:           exp;                                       \
      case 5:           exp;                                       \
      case 4:           exp;                                       \
      case 3:           exp;                                       \
      case 2:           exp;                                       \
      case 1:           exp;                                       \
      case 0:           break;                                     \
    }                                                              \
    for ( mcj = 0; mcj < mcn; mcj++) {                             \
      exp; exp; exp; exp; exp; exp; exp; exp;                      \
    }                                                              \
    exp_next_line;                                                 \
  }                                                                \
} while(0)

#if 1
void write_32_to_32(unsigned int *dst, unsigned int *src, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1) {
	int step0, step1;

	if (x_offset <= 0 || y_offset <= 0)
		return;

	step0 = pixels_line0 - x_offset;
	step1 = pixels_line1 - x_offset;

	lcd_copy_common(
		x_offset,
		y_offset,
		{*dst++ = *src++;},
		{dst += step0; src += step1;});
}
#else
void write_32_to_32(unsigned int *dst, unsigned int *src, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1) {
	int i;

	x_offset = max(x_offset, 0);
	y_offset = max(y_offset, 0);

	for (i = 0; i < y_offset; ++i) {
		memcpy(base0, base1, x_offset * 4);
		base0 += pixels_line0;
		base1 += pixels_line1;
	}
}
#endif

void write_32_to_32_alpha(unsigned int *dst, unsigned int *src, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1, unsigned int alpha_color) {
	int step0, step1;

	if (x_offset <= 0 || y_offset <= 0)
		return;

	step0 = pixels_line0 - x_offset;
	step1 = pixels_line1 - x_offset;

	lcd_copy_common(
		x_offset,
		y_offset,
		MEMCPY1_ALPHA(dst, src, alpha_color),
		{dst += step0; src += step1;});
}

void write_32_to_32_alpha_clear(unsigned int *dst, unsigned int *src, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1, unsigned int alpha_color, unsigned int bg_color) {
	int step0, step1;

	if (x_offset <= 0 || y_offset <= 0)
		return;

	step0 = pixels_line0 - x_offset;
	step1 = pixels_line1 - x_offset;

	lcd_copy_common(
		x_offset,
		y_offset,
		MEMCPY1_ALPHA_CLEAR(dst, src, alpha_color, bg_color),
		{dst += step0; src += step1;});
}

void write_32_to_32_alpha_replace(unsigned int *dst, unsigned int *src, unsigned int *bg_pic,
	int x_offset, int y_offset, int pixels_line0, int pixels_line1, int pixels_line2,
	unsigned int alpha_color, unsigned int replace_color) {
	int step0, step1, step2;

	if (x_offset <= 0 || y_offset <= 0)
		return;

	step0 = pixels_line0 - x_offset;
	step1 = pixels_line1 - x_offset;
	step2 = pixels_line2 - x_offset;

	lcd_copy_common(
		x_offset,
		y_offset,
		MEMCPY1_ALPHA_REPLACE(dst, src, bg_pic, alpha_color, replace_color),
		{dst += step0; src += step1; bg_pic += step2;});
}

void write_32_to_32_alpha_replace_src(unsigned int *dst, unsigned int *src, unsigned int *bg_pic,
       int x_offset, int y_offset, int pixels_line0, int pixels_line1, int pixels_line2,
       unsigned int alpha_color) {
	int step0, step1, step2;

	if (x_offset <= 0 || y_offset <= 0)
		return;

	step0 = pixels_line0 - x_offset;
	step1 = pixels_line1 - x_offset;
	step2 = pixels_line2 - x_offset;

	lcd_copy_common(
		x_offset,
		y_offset,
		MEMCPY1_ALPHA_REPLACE_SRC(dst, src, bg_pic, alpha_color),
		{dst += step0; src += step1; bg_pic += step2;});
}


void clear_mem32(unsigned int *base, int xres, int yres, int pixels_per_line, unsigned int color) {
	int step;

	if (xres <= 0 || yres <= 0)
		return;

	step =  pixels_per_line - xres;

	lcd_copy_common(
		xres,
		yres,
		{*base++ = color;},
		{base += step;});
}

void clear_mem32_hor(unsigned int *base, int xres, int yres, int pixels_per_line,
	int hor, unsigned int color1, unsigned int color2) {
	if (hor > xres) hor = xres;

	clear_mem32(base, hor, yres, pixels_per_line, color1);
	clear_mem32(base + hor, xres - hor, yres, pixels_per_line, color2);
}

void clear_mem32_ver(unsigned int *base, int xres, int yres, int pixels_per_line,
	int ver, unsigned int color1, unsigned int color2) {
	if (ver > yres) ver = yres;
	
	clear_mem32(base, xres, ver, pixels_per_line, color1);
	clear_mem32(base + ver * pixels_per_line, xres, yres - ver, pixels_per_line, color2);
}

void dump_mem32_str(unsigned int *base, int xres, int yres, int pixels_per_line, const char *str, int line_len) {
	int i, j, count;

	xres = max(xres, 0);
	yres = max(yres, 0);

	for (i = 0, count = 0; i < yres; ++i) {
		for (j = 0; j < xres; ++j, ++count) {
			if (!(count % line_len))
				pr_info("\n");
			pr_info(str, base[j]);
		}
		base += pixels_per_line;
	}
	pr_info("\n");
}
