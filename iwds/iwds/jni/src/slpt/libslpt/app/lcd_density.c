#include <errno.h>
#include <linux/fb.h>
#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
#include <cutils/properties.h>
#endif
#include <math.h>

#include <common.h>

#define  LCD_DENSITY_LDPI      120
#define  LCD_DENSITY_MDPI      160
#define  LCD_DENSITY_TVDPI     213
#define  LCD_DENSITY_HDPI      240
#define  LCD_DENSITY_XHDPI     320
#define  LCD_DENSITY_XXHDPI    480
#define  LCD_DENSITY_XXXHDPI   640

#define SMALLEST_SCREEN_DIAGONAL 2.8

#define LCD_DENSITY_PROPERTY "qemu.sf.lcd_density"

extern int fb_init(void);
extern struct fb_var_screeninfo *get_default_fb_var(void);

unsigned int lcd_density(unsigned int xres, unsigned int yres,
                         unsigned int width, unsigned int height) {
	double diagonal;
	unsigned int density;

	pr_info("LCD DENSITY: (x, y) (%d, %d) , (w, h) (%d, %d)\n", xres, yres, width, height);

	diagonal = sqrt(width * width + height * height);
	diagonal *= 0.0393701;

	density = sqrt(xres * xres + yres *yres) / diagonal;

	pr_info("LCD DENSITY: diagonal: %f   density: %d\n", diagonal, density);

	if (diagonal < SMALLEST_SCREEN_DIAGONAL && (density >=  (LCD_DENSITY_MDPI + LCD_DENSITY_TVDPI) / 2)) {
		density = sqrt(xres * xres + yres *yres) / SMALLEST_SCREEN_DIAGONAL;
		pr_info("LCD DENSITY: adapt to diagonal: %f   density: %d\n", SMALLEST_SCREEN_DIAGONAL, density);
	}

	if (density < (LCD_DENSITY_LDPI + LCD_DENSITY_MDPI)/2)
		density = LCD_DENSITY_LDPI;
	else if (density < (LCD_DENSITY_MDPI + LCD_DENSITY_TVDPI)/2)
		density = LCD_DENSITY_MDPI;
	else if (density < (LCD_DENSITY_TVDPI + LCD_DENSITY_HDPI)/2)
		density = LCD_DENSITY_TVDPI;
	else if (density < (LCD_DENSITY_HDPI + LCD_DENSITY_XHDPI)/2)
		density = LCD_DENSITY_HDPI;
	else if (density < (LCD_DENSITY_XHDPI + LCD_DENSITY_XXHDPI)/2)
		density = LCD_DENSITY_XHDPI;
	else if (density < (LCD_DENSITY_XXHDPI + LCD_DENSITY_XXXHDPI)/2)
		density = LCD_DENSITY_XXHDPI;
	else
		density = LCD_DENSITY_XXXHDPI;

	pr_info("LCD DENSITY: result is: %d\n", density);

	return density;
}

int lcd_density_main(int argc, char **argv) {
	struct fb_var_screeninfo *info;
	int ret = fb_init();
	unsigned int density;
	char buf[20];

	if (ret < 0) {
		pr_err("LCD DENSITY: failed to init fb\n");
		return ret;
	}

	info = get_default_fb_var();
	if (!info) {
		pr_err("LCD DENSITY: failed to init fb\n");
		return -ENODEV;
	}

	if (!info->width || !info->height) {
		pr_err("LCD DENSITY: device not identify it's actual physical size\n");
		return -EINVAL;
	}

	density = lcd_density(info->xres, info->yres, info->width, info->height);

	sprintf(buf, "%d", density);

#ifdef CONFIG_SLPT_LINUX_EXCUTABLE
	ret = property_set(LCD_DENSITY_PROPERTY, buf);
	if (ret < 0) {
		pr_err("LCD DENSITY: property set failed: %s\n", LCD_DENSITY_PROPERTY);
		return ret;
	}
#endif

	return 0;
}
