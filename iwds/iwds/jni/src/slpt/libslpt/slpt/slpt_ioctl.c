#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <common.h>

/* for slpt kernel driver */
#define SLPT_CMD_LOAD_FW     _IOW('S', 0x121, int)
#define SLPT_CMD_ENABLE_FW   _IOW('S', 0x122, int)
#define SLPT_CMD_DISABLE_FW  _IOW('S', 0x123, int)

/* for slpt */
#define	SLPT_IOCTL_INIT_SVIEW         _IOW('s', 0x121, int)
#define	SLPT_IOCTL_CLEAR_PICTURE_GRP  _IOW('s', 0x122, int)
#define	SLPT_IOCTL_ADD_PICTURE_GRP    _IOW('s', 0x123, int)
#define	SLPT_IOCTL_ADD_PICTURE        _IOW('s', 0x124, int)

#define TAG_SIZE 4
struct slpt_data {
	char tag[TAG_SIZE];
	unsigned int hdr_len;
	unsigned int mem_len;
	void *hdr;
	void *mem;
};

static int fd = -1;

int slpt_ioctl(void *hdr, unsigned int hdr_len, void *mem, unsigned int mem_len, unsigned int cmd)
{
	struct slpt_data data;

	data.tag[0] = 'S';
	data.tag[1] = 'L';
	data.tag[2] = 'P';
	data.tag[3] = 'T';
	data.hdr_len = hdr_len;
	data.mem_len = mem_len;
	data.hdr = hdr;
	data.mem = mem;

	if (fd < 0) {
		fd = open("/dev/slpt", O_RDWR);
		if (fd < 0) {
			pr_err("open failed because: %s\n", strerror(errno));
			return -ENODEV;
		}
	}

	return ioctl(fd, cmd, &data);
}

void slpt_ioctl_close(void)
{
	if (fd >= 0)
		close(fd);

	fd = -1;
}

static int firmware[] = {
#include "../bin/slpt_default_firmware.hex"
};
static char *desc = "slpt-app";

/* those functions are called to control slpt kernel driver */
int slpt_ioctl_load_default_firmware(void)
{
	return slpt_ioctl(desc, strlen(desc) + 1, firmware, sizeof(firmware), SLPT_CMD_LOAD_FW);
}

int slpt_ioctl_enable_default_task(void)
{
	return slpt_ioctl(desc, strlen(desc) + 1, NULL, 0, SLPT_CMD_ENABLE_FW);
}

int slpt_ioctl_disable_default_task(void)
{
	return slpt_ioctl(desc, strlen(desc) + 1, NULL, 0, SLPT_CMD_DISABLE_FW);
}


/* those functions are called to control slpt */
int slpt_ioctl_init_sview(void *mem, unsigned int size) {
	return slpt_ioctl(NULL, 0, mem, size, SLPT_IOCTL_INIT_SVIEW);
}

int slpt_ioctl_clear_picture_grp(void) {
	return slpt_ioctl(NULL, 0, NULL, 0, SLPT_IOCTL_CLEAR_PICTURE_GRP);
}

int slpt_ioctl_add_picture_grp(const char *grp_name) {
	return slpt_ioctl(NULL, 0, (void *)grp_name, strlen(grp_name) + 1, SLPT_IOCTL_ADD_PICTURE_GRP);
}

int slpt_ioctl_add_picture(const char *pic_name, void *mem, unsigned int size) {
	return slpt_ioctl((void *)pic_name, strlen(pic_name) + 1, mem, size, SLPT_IOCTL_ADD_PICTURE);
}
