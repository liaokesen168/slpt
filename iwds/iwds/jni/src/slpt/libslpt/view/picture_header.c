#include <common.h>
#include <list.h>
#include <picture.h>
#include <view.h>

#include <utime.h>
#include <file_ops.h>

int check_picture_header(struct picture_header *header) {
	struct fb_region *region;
	unsigned int len;

	if (strcmp(header->tag, PICTURE_TAG)) {
		pr_err("picture header: not a valid tag: [%s]\n", header->tag);
		return -EINVAL;
	}

	region = &header->region;
	if (header->xres != region->xres || header->yres != region->yres) {
		pr_err("picture header: second check not passed: (%d %d), (%d %d)\n",
		       header->xres, header->yres, region->xres, region->yres);
		return -EINVAL;
	}

	len = region_length(region);
	if (header->len != (len + sizeof(*header))) {
		pr_err("picture header: third check not passed: (%d %d %d), (%u %u)\n",
		       region->xres, region->yres, region->pixels_per_line, len, header->len);
		return -EINVAL;
	}

	region->base = header->mem;

	return 0;
}

int picture_sync_no_bmp(struct picture *pic) {
	struct picture_header *header = (struct picture_header *) pic->buffer;
	int ret;

	ret = check_picture_header(header);
	pic->region = !ret ? &header->region : NULL;

	return ret;
}

struct picture_header *create_picture_header(char *buffer) {
	struct picture_header *header;
	struct fb_region *region = NULL;

	header = malloc(sizeof(*header));
	if (!header) {
		pr_err("picture header: failed to allocate header struct!\n");
		return NULL;
	}

	bmp_file_to_fb_region(buffer, &region);
	if (!region) {
		pr_err("picture header: not a vaild bmp file!\n");
		free(header);
		return NULL;
	}

	strcpy(header->tag, PICTURE_ERR_TAG);
	header->xres = region->xres;
	header->yres = region->yres;
	header->len = sizeof(*header) + region_length(region);
	header->region = *region;
	header->region.base = region;

	return header;
}

void free_picture_header(struct picture_header *header) {
	struct fb_region *region;

	if (header) {
		region = (struct fb_region *) header->region.base;
		if (region) {
			if (region->base)
				free(region->base);
			free(region);
		}
		free(header);
	}
}

int write_picture_header(const char *path, struct picture_header *header) {
	int fd;
	int ret = 0;
	unsigned int len;
	struct fb_region *region;
	
	if (!path || !header)
		return -EINVAL;

	fd = open(path, O_WRONLY | O_SYNC);
	if (fd < 0)
		return fd;

	if (write_file_by_fd(fd, (void *)header, sizeof(*header)) != sizeof(*header)) {
		ret = -1;
		goto close_fd;
	}

	region = (struct fb_region *) header->region.base;
	len = region_length(region);

	if (write_file_by_fd(fd, region->base, len) != (ssize_t)len) {
		ret = -1;
		goto close_fd;
	}

	if (lseek(fd, 0, SEEK_SET) != 0) {
		ret = -1;
		goto close_fd;
	}

	if (write_file_by_fd(fd, PICTURE_TAG, PICTURE_TAG_SIZE) != PICTURE_TAG_SIZE) {
		ret = -1;
		goto close_fd;
	}

	utime(path, NULL);

close_fd:
	close(fd);

	return ret;
}

int write_bmp_by_picture_header(const char *path, char *buffer) {
	struct picture_header *header;
	int ret;

	if (!path || !buffer)
		return -EINVAL;

	header = create_picture_header(buffer);
	if (!header)
		return -EINVAL;

	ret = write_picture_header(path, header);
	
	free_picture_header(header);

	return ret;
}
