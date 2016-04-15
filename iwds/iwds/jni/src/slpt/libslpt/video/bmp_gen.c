#include "bmp_logo.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <common.h>

enum bf_off{
	BF_TYPE_OFF = 0x00,
	BF_SIZE_OFF = 0x02,
	BF_OFFBITS_OFF = 0x0a,
	BI_SIZE_OFF = 0X0E,
	BI_WIDTH_OFF = 0X12,
	BI_HEIGHT_OFF = 0X16,
	BI_BITCOUNT_OFF = 0X1C,
	BI_COMPRESSION_OFF = 0X1E,
	BI_CLRUSED_OFF = 0X2E,
	BI_CLRIMPORTANT_OFF = 0X32,

};

enum bf_size{
	BF_TYPE_SIZE = 2,
	BF_SIZE_SIZE = 4,
	BF_OFFBITS_SIZE = 4,
	BI_SIZE_SIZE = 4,
	BI_WIDTH_SIZE = 4,
	BI_HEIGHT_SIZE = 4,
	BI_BITCOUNT_SIZE = 2,
	BI_COMPRESSION_SIZE = 4,
	BI_CLRUSED_SIZE = 4,
	BI_CLRIMPORTANT_SIZE = 4,

	BF_FILEHEADER_SIZE = 14,
};

static unsigned int BF_PALETTE_SIZE = 0;
static unsigned int BF_PALETTE_OFF = 0;
static unsigned int BI_INFOHEADER_SIZE = 40;

#define bmp_swap4(x) (x)

#define type_supported(type) (type == ('B' | 'M' << 8))

#define compression_supported(compression) (compression == 0)

#define info_supported(infosize) (infosize == BI_INFOHEADER_SIZE)

#define print_val(x) pr_info ("%s:\t0x%x \t%u\n",#x, (unsigned int)x,  (unsigned int)x)

#ifndef ARRAY_SIZE
#define ARRAY_SIZE(x) (sizeof(x)/sizeof(x[1]))
#endif

#define to_line_len(width, bitcount) ((((width) * (bitcount) + 31) & ~31) / 8 )

inline void fill_data(unsigned char *filebuffer, unsigned int offset, unsigned int size, unsigned int *data) {
	*data = 0;
	memcpy(data, filebuffer + offset, size);
}

#define for_each_bmp_line(bmp, src, i, _height, line_len)				\
	for (i = 0, src = bmp->height > 0 ? bmp->databuffer + (_height - 1) * line_len : bmp->databuffer \
			 ; i < _height;												\
		 ++i, (bmp->height > 0 ? (src -= line_len) : (src += line_len)))

#define color32_red(color)   (((color) & 0x00ff0000) >> 16)
#define color32_green(color) (((color) & 0x0000ff00) >> 8)
#define color32_blue(color)  (((color) & 0x000000ff) >> 0)

#define color32_to_16(color)					\
	(((color32_red(color) >> 3) << 11) |		\
	 ((color32_green(color) >> 2) << 5) |		\
	 ((color32_red(color) >> 3) << 0) )

#define color16_red(color)  (((color) & 0xf800) >> 11)
#define color16_green(color) (((color) & 0x0fe0)>> 5)
#define color16_blue(color) (((color) & 0x1f) >> 0)

#define color16_to_32(color)					\
	((color16_red(color) << 16) |				\
	 (color16_green(color) << 8) |				\
	 (color16_blue(color) << 0))

void bmp_bits_to_color(struct bmp_logo_info *bmp, void *base, int bpp) {
	 int line_len;
	 int _height, width, bitcount;
	unsigned char *src;
	unsigned char *p;
	unsigned char bitsmask;
	unsigned int *palette;
	int i, j, k;
	unsigned int bitsvalue;
	unsigned int colorvalue;
	unsigned int *p32;
	unsigned short *p16;

	bitcount = bmp->bitcount;
	switch (bitcount) {
	case 1: bitsmask = 0x01; break;
	case 2: bitsmask = 0x03; break;
	case 4: bitsmask = 0x0F; break;
	case 8: bitsmask = 0xFF; break;
	default:
		pr_err( "Couldn't support this kind of bitcount:%x\n",bitcount);
		return;
	}

	_height = bmp_height(bmp->height);
	width = bmp->width;
	bitcount = bmp->bitcount;
	line_len = to_line_len(width, bitcount);
	palette = bmp->palette;

	src = bmp->height > 0 ? bmp->databuffer + (_height - 1) * line_len : bmp->databuffer;

	switch (bpp) {
	case 32: case 30: case 24: case 18:
		p32 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, p = src; ; ++p) {
				for (k = 8 - bitcount; k >= 0 ; k-=bitcount, ++j, ++p32) {
					if (!(j < width))
						goto next_line32;
					bitsvalue = (*p >> k) & bitsmask;
					colorvalue = palette[bitsvalue];
					*p32 = colorvalue;
				}
			}
		next_line32:
			;
		}
		break;
	case 16: case 15:
		p16 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, p = src; ; ++p) {
				for (k = 8 - bitcount; k >= 0 ; k-=bitcount, ++j, ++p16) {
					if (!(j < width))
						goto next_line16;
					bitsvalue = (*p >> k) & bitsmask;
					colorvalue = palette[bitsvalue];
					*p16 = color32_to_16(colorvalue);
				}
			}
		next_line16:
			;
		}
		break;
	default:
		pr_err( "Couldn't support this kind of bpp:%x\n", bpp);
		break;
	}
}

void bmp_bytes_to_color(struct bmp_logo_info *bmp, void *base, int bpp) {
	int line_len;
	int _height, width, bitcount;
	unsigned char *src;
	unsigned char *p;
	unsigned char *dest;
	int i, j, bytesnum;
	unsigned int *p32, *s32;
	unsigned short *p16, *s16;
	int _bpp;

	bitcount = bmp->bitcount;
	switch (bitcount) {
	case 16: bytesnum = 2; break;
	case 24: bytesnum = 3; break;
	case 32: bytesnum = 4; break;
	default:
		pr_err( "Couldn't support this kind of bitcount:%x\n",bitcount);
		return;
	}

	switch (bpp) {
	case 32: case 30: case 24: case 18:
		_bpp = 32; break;
	case 16: case 15:
		_bpp = 16; break;
	default:
		pr_err( "Couldn't support this kind of bpp:%x\n", bpp);
		return ;
	}

	_height = bmp_height(bmp->height);
	width = bmp->width;
	bitcount = bmp->bitcount;
	line_len = to_line_len(width, bitcount);
	p16 = base;
	p32 = base;

	s16 = (void *)bmp->databuffer;
	s32 = (void *)bmp->databuffer;
	src = bmp->height > 0 ? bmp->databuffer + (_height - 1) * line_len : bmp->databuffer;

	if ((_bpp == 16 && bitcount == 16) || (_bpp == 32 && bitcount == 32)) {
		dest = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			memcpy(dest, src, width * bytesnum);
			dest += width * bytesnum;
		}
	}  else if (_bpp == 16 && bitcount == 32) {
		p16 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, s32 = (unsigned int *)src; j < width; ++j, ++s32, ++p16) {
				*p16 = color32_to_16(*s32);
			}
		}
	} else if (_bpp == 16 && bitcount == 24) {
		p16 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, p = src; j < width; ++j, p += 3, ++p16) {
				*p16 = color32_to_16(*((unsigned int *)p));
			}
		}
	} else if (_bpp == 32 && bitcount == 16 ) {
		p32 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, s16 = (unsigned short*)src; j < width; ++j, ++s16, ++p32) {
				*p32 = color16_to_32(*s16);
			}
		}
	} else if (_bpp == 32 && bitcount == 24) {
		p32 = base;
		for_each_bmp_line(bmp, src, i, _height, line_len) {
			for (j = 0, p = src; j < width; ++j, p += 3, ++p32) {
				*p32 = p[0] + (p[1] << 8) + (p[2] << 16);
			}
		}
	}
}

#define bmp1_to_color(bmp, base, bpp) bmp_bits_to_color(bmp, base, bpp)
#define bmp2_to_color(bmp, base, bpp) bmp_bits_to_color(bmp, base, bpp)
#define bmp4_to_color(bmp, base, bpp) bmp_bits_to_color(bmp, base, bpp)
#define bmp8_to_color(bmp, base, bpp) bmp_bits_to_color(bmp, base, bpp)
#define bmp16_to_color(bmp, base, bpp) bmp_bytes_to_color(bmp, base, bpp)
#define bmp24_to_color(bmp, base, bpp) bmp_bytes_to_color(bmp, base, bpp)
#define bmp32_to_color(bmp, base, bpp) bmp_bytes_to_color(bmp, base, bpp)

void bmp_to_color(struct bmp_logo_info *bmp, void *base, unsigned int bpp) {
	unsigned int bitcount;

	bitcount = bmp->bitcount;
	switch (bitcount) {
	case 1:
		bmp1_to_color(bmp, base, bpp); break;
	case 2:
		bmp2_to_color(bmp, base, bpp); break;
	case 4:
		bmp4_to_color(bmp, base, bpp); break;
	case 8:
		bmp8_to_color(bmp, base, bpp); break;
	case 16:
		bmp16_to_color(bmp, base, bpp); break;
	case 24:
		bmp24_to_color(bmp, base, bpp); break;
	case 32:
		bmp32_to_color(bmp, base, bpp); break;
	default:
		pr_err( "Could not support this bitcount:%u\n",bitcount);
		return;
	}
}

int bmp_file_to_color_mode(void *file_buf, struct color_mode **colors) {
	struct bmp_logo_info *bmp;
	struct color_mode *new_colors;

	if (((unsigned char *)file_buf)[0] != 0 && ((unsigned char *)file_buf)[1] != 0) {
		bmp = bmp_deal_filebuffer(file_buf);
		if (bmp) {
			new_colors = bmp_to_color_mode(bmp, 4);
			if (new_colors) {
				if (*colors)
					free(*colors);
				*colors = new_colors;
				((unsigned char *)file_buf)[0] = 0;
				((unsigned char *)file_buf)[1] = 0;
				free_bmp(bmp);
				return 0;
			}
			free_bmp(bmp);
		}
	}

	return -1;
}

struct color_mode* bmp_to_color_mode(struct bmp_logo_info *bmp, unsigned int color_bytes) {
	struct color_mode*mode;

	mode = (struct color_mode*)malloc(sizeof(struct color_mode) +
			 bmp_height(bmp->height) * bmp->width * color_bytes);
	if (mode == NULL) {
		pr_err( "Failed to allocate color memory\n");
		return NULL;
	}

	mode->height = bmp_height(bmp->height);
	mode->width = bmp->width;
	mode->bpp = color_bytes * 8;

	bmp_to_color(bmp, mode->base, mode->bpp);

	return mode;
}

int bmp_file_to_fb_region(void *file_buf, struct fb_region **region) {
	struct bmp_logo_info *bmp;
	struct fb_region *new_region;

	if (((unsigned char *)file_buf)[0] != 0 && ((unsigned char *)file_buf)[1] != 0) {
		bmp = bmp_deal_filebuffer(file_buf);
		if (bmp) {
			new_region = bmp_to_fb_region(bmp, 4);
			if (new_region) {
				if (*region) {
					if ((*region)->base)
						free((*region)->base);
					free(*region);
				}
				*region = new_region;
				((unsigned char *)file_buf)[0] = 0;
				((unsigned char *)file_buf)[1] = 0;
				free_bmp(bmp);
				return 0;
			}
			free_bmp(bmp);
		}
	}

	return -1;
}

struct fb_region* bmp_to_fb_region(struct bmp_logo_info *bmp, unsigned int color_bytes) {
	struct fb_region *region;

	region = (struct fb_region *)malloc(sizeof(struct fb_region));
	if (region == NULL) {
		pr_err( "Failed to allocate region struct\n");
		return NULL;
	}

	region->base = malloc(bmp_height(bmp->height) * bmp->width * color_bytes);
	if (!region->base) {
		pr_err( "Failed to allocate region memory\n");
		free(region);
		return NULL;
	}
	region->yres = bmp_height(bmp->height);
	region->xres = bmp->width;
	region->bpp = color_bytes * 8;
	region->pixels_per_line = region->xres;

	bmp_to_color(bmp, region->base, region->bpp);

	return region;
}

struct bmp_logo_info* bmp_deal_filebuffer(void *filebuffer) {
	unsigned int type, filesize, dataoffset;
	int height;
	unsigned int infosize, width, bitcount, compression, clrused, clrimportant;

	fill_data(filebuffer, BF_TYPE_OFF, BF_TYPE_SIZE, &type);
	fill_data(filebuffer, BF_SIZE_OFF, BF_SIZE_SIZE, &filesize);
	fill_data(filebuffer, BF_OFFBITS_OFF, BF_OFFBITS_SIZE, &dataoffset);

	fill_data(filebuffer, BI_WIDTH_OFF, BI_WIDTH_SIZE, &width);
	fill_data(filebuffer, BI_HEIGHT_OFF, BI_HEIGHT_SIZE, (unsigned int *)&height);
	fill_data(filebuffer, BI_SIZE_OFF, BI_SIZE_SIZE, &infosize);
	fill_data(filebuffer, BI_BITCOUNT_OFF, BI_BITCOUNT_SIZE, &bitcount);
	fill_data(filebuffer, BI_COMPRESSION_OFF, BI_COMPRESSION_SIZE, &compression);
	fill_data(filebuffer, BI_CLRUSED_OFF, BI_CLRUSED_SIZE, &clrused);
	fill_data(filebuffer, BI_CLRIMPORTANT_OFF, BI_CLRIMPORTANT_SIZE, &clrimportant);

	if (!type_supported(type)) {
		pr_err( "Can't support such type:\"%c%c(0x%x)\"\n",type % 256, type / 256, type);
		return NULL;
	}
	if (!compression_supported(compression)) {
		pr_err( "Can't support such compression method:0x%x\n",compression);
		return NULL;
	}
	if (!info_supported(infosize)) {
		pr_err( "Can't support this info header with this size:0x%x\n",infosize);
		return NULL;
	}

	struct bmp_logo_info *bmp;

	bmp = malloc(sizeof(struct bmp_logo_info));
	if (bmp == NULL) {
		pr_err( "Failed to allocate bmp logo info struct");
		return NULL;
	}

	BF_PALETTE_OFF = BF_FILEHEADER_SIZE + infosize;
	BF_PALETTE_SIZE = dataoffset - BF_PALETTE_OFF;

	bmp->type = type;
	bmp->compression = compression;
	bmp->width = width;
	bmp->height = height;
	bmp->bitcount = bitcount;
	bmp->nclrs = clrused > clrimportant ? clrused : clrimportant;
	bmp->nclrs = (bmp->nclrs == 0 ? BF_PALETTE_SIZE / 4 : bmp->nclrs);
#if CONFIG_BMP_DATA_ALLOCATE
	bmp->palette = (unsigned int *)malloc(BF_PALETTE_SIZE);
	if (bmp->palette == NULL) {
		pr_err( "Failed to allocate bmp palette buffer\n");
		return NULL;
	}
	memcpy(bmp->palette, (char *)filebuffer + BF_PALETTE_OFF, BF_PALETTE_SIZE);
#else
	bmp->palette = (unsigned int *)(filebuffer + BF_PALETTE_OFF);
#endif
#if CONFIG_BMP_DATA_ALLOCATE
	int ii,  _height;
	unsigned int line_len;
	unsigned char *databuf;
	unsigned char *dest;
	unsigned char *src;

	bmp->databuffer = (unsigned char *)malloc(filesize - dataoffset);
	if (bmp->databuffer == NULL) {
		pr_err( "Failed to allocate bmp data buffer\n");
		return NULL;
	}
	databuf = (unsigned char *)filebuffer + dataoffset;
	line_len = to_line_len(bmp->width, bmp->bitcount);
	_height = bmp_height(bmp->height);
	/*For bmp file
	 *   if height > 0 , the data store order is From down to up, left to right
	 *      height < 0 , the data store order is From up to down, left to right.
	 *
	 * we want the data suite for the store oder of framebuffer
	 *     From up to down, left to right.
	 */
	src = bmp->height > 0 ? databuf + (_height - 1) * line_len : databuf;
	dest = bmp->databuffer;
	for ( ii = 0; ii < _height; ++ii) {
		memcpy(dest, src, line_len);
		if (bmp->height > 0)
			src -= line_len;
		else
			src += line_len;
		dest += line_len;
	}
	/* So we have a Negative bmp height (heigt < 0) */
	bmp->height = 0 - bmp_height(bmp->height);
#else
	bmp->databuffer = filebuffer + dataoffset;
#endif

	return bmp;
}

void free_bmp(struct bmp_logo_info *bmp) {
	if (bmp) {
#if CONFIG_BMP_DATA_ALLOCATE
		free(bmp->palette);
		free(bmp->databuffer);
#endif
		free(bmp);
	}
}

#ifdef BMP_SUPPORT_FILE_OP
struct bmp_logo_info* bmp_deal_file(char *filename){
	FILE *fp;
	struct bmp_logo_info *bmp = NULL;

	fp = fopen(filename, "rb");
	if (fp == NULL) {
		pr_err( "Failed to open file \"%s\"\n",filename);
		return NULL;
	}

	char type1, type2;
	type1 = fgetc(fp);
	type2 = fgetc(fp);
	if (type1 != 'B' || type2 != 'M') {
		pr_err( "Don't support this kind of bmp file:\"%c%c\"\n",type1, type2);
		goto close_file;
	}

	unsigned int file_len = 0;
	if (fread(&file_len, BF_SIZE_SIZE, 1, fp) != 1) {
		pr_err( "Couldn't read bmp file size\n");
		goto close_file;
	}

	char *filebuffer;
	filebuffer = (char *)malloc(file_len);
	if (filebuffer == NULL) {
		pr_err( "Failed to allocate the file buffer\n");
		goto close_file;
	}

	fseek(fp, 0, SEEK_SET);
	if (fread(filebuffer, file_len, 1, fp) != 1) {
		pr_err( "Couldn't read the whole bmp file\n");
		goto free_buffer;
	}

	/*
	 * This should not be exist, but I want copy the bmp_deal_filebuffer() to another place.
	 */
	bmp = bmp_deal_filebuffer(filebuffer);

free_buffer:
	free(filebuffer);
close_file:
	fclose(fp);

	return bmp;
}
#endif
