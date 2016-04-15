#include <fcntl.h>
#include <asm/errno.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "slpt.h"
#include <common.h>
#include <call_app.h>

#include <sys/time.h>
#include <getopt.h>

#include <file_ops.h>

char *slpt_get_resfilename( const char *path, const char *app_name, char *buf) {
	if (!path)
		return 0;

	if (!buf) {
		buf = malloc(MAX_FILE_NAME);
		if (!buf) {
			return 0;
		}
	}

	sprintf(buf, "%s/apps/%s/res/%s/data", SLPT_ROOT_DIR, app_name ? app_name : SLPT_DEFAULT_APP, path);

	return buf;
}
