#ifndef _UBOOT_TEST_LCD_LOGO_BMP_H_
#define _UBOOT_TEST_LCD_LOGO_BMP_H_

#define BMP_SUPPORT_FILE_OP 1

#define CONFIG_BMP_DATA_ALLOCATE 1

#ifndef COLOR_MODE_STRUCT
#define COLOR_MODE_STRUCT
struct color_mode{
	unsigned int width;
	unsigned int height;
	unsigned int bpp;
	unsigned char base[0];
};
#endif /* COLOR_MODE_STRUCT */

#ifndef FB_REGION_STRUCT
#define FB_REGION_STRUCT
struct fb_region {
	void *base;
	unsigned int xres;
	unsigned int yres;
	unsigned int pixels_per_line;
	unsigned int bpp;
};
#endif	/* COLOR_MODE_STRUCT */

#ifndef BMP_LOGO_INGO_STRUCT
#define BMP_LOGO_INGO_STRUCT
struct bmp_logo_info{
	unsigned int type;
	unsigned int compression;
	unsigned int width;
	int height;
	unsigned int bitcount;
	unsigned int nclrs;
	unsigned int *palette;
	unsigned char *databuffer;
};
#endif /* BMP_LOGO_INGO_STRUCT */

void bmp_to_color(struct bmp_logo_info *bmp, void *base, unsigned int bpp);
struct color_mode* bmp_to_color_mode(struct bmp_logo_info *bmp, unsigned int color_bytes);
struct fb_region* bmp_to_fb_region(struct bmp_logo_info *bmp, unsigned int color_bytes);
int bmp_file_to_color_mode(void *file_buf, struct color_mode **colors);
int bmp_file_to_fb_region(void *file_buf, struct fb_region **region);

struct bmp_logo_info* bmp_deal_filebuffer(void *filebuffer);
void free_bmp(struct bmp_logo_info *bmp);
#if BMP_SUPPORT_FILE_OP
struct bmp_logo_info* bmp_deal_file(char *filename);
#else
static inline struct bmp_logo_info* bmp_deal_file(char *filename) {
	return (struct bmp_logo_info*)0;
}
#endif

#define bmp_height(height) ((height) > 0 ? (height) : (0 - height))

#endif /* _UBOOT_TEST_LCD_LOGO_BMP_H_ */
