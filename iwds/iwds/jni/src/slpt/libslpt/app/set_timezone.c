#include <stdio.h>
#include <sys/time.h>
#include <getopt.h>

#include <common.h>
#include <file_ops.h>
#include <rtc_time.h>
#include "slpt.h"

enum {
	A_TZ_IGNORE_DST = 0,
	A_TZ_USE_SECS,

	/* keep last */
	A_TZ_TMP,				/* for {0, 0, 0, 0}, */
	A_TZ_NUMS,
};

void print_args(int argc, char **argv) {
	int i;

	if (!argv) {
		return;
	}

	for (i = 0; i < argc; ++i) {
		pr_info ("%s ", argv[i]);
	}
	pr_info ("\n");
}

int set_timezone_main(int argc, char **argv) {
	int opt = -1;
	int option_index = -1;
	struct option long_options[A_TZ_NUMS] = {
		[A_TZ_IGNORE_DST] = {"ignore_dsttime", no_argument, 0, 0},
		[A_TZ_USE_SECS] = {"use_secs", no_argument, 0, 0},
		[A_TZ_TMP] = {0, 0, 0, 0},
	};
	int flags[A_TZ_NUMS] = {0};
	char *path;
	int ret;

	print_args(argc, argv);

	while ((opt = getopt_long(argc, argv, "", long_options, &option_index)) != -1) {
		switch (opt) {
		case 0:
			flags[option_index] = 1;
			break;
		default:
			pr_err("Not valid arguments: opt:0x%x  index:%d arg:%s errno:%d\n",
				   opt, option_index, optarg, errno);
			return -EINVAL;
		}
	}

	path = (optind >= argc) ? slpt_get_resfilename(SLPT_FILE_TIMEZONE, NULL, NULL) : argv[optind];

	struct timeval timeval;
	struct timezone timezone;
	char buf[4] = {0};
	int offset;

	ret = gettimeofday(&timeval, &timezone);
	if (ret < 0) {
		pr_err("Failed to get time of day\n");
		return ret;
	}

	offset = timezone.tz_minuteswest;
	if (flags[A_TZ_IGNORE_DST])
		offset += timezone.tz_dsttime;

	if (flags[A_TZ_USE_SECS])
		offset *= 60;

	offset *= -1;

	buf[0] = offset & 0xff;
	buf[1] = (offset >> 8) & 0xff;
	buf[2] = (offset >> 16) & 0xff;
	buf[3] = (offset >> 24) & 0xff;

	ret = write_file(path, buf, sizeof(buf));
	if (ret) {
		pr_err("%s: failed to write file: %s\n", __FUNCTION__, path);
		return ret;
	}

	pr_info("time tv_sec:%ld\n", timeval.tv_sec);
	pr_info("timezone: %d minutes dsttime: %d\n", timezone.tz_minuteswest, timezone.tz_dsttime);
	pr_info("time offset:(%d %s) wirte to %s\n", offset, flags[A_TZ_USE_SECS] ? "seconds" : "minutes", path);

	struct rtc_time tm;

	/* get_display_tm(&tm); */
	pr_info("date: %d-%02d-%02d week:%d time: %02d:%02d sec:%02d\n",
			tm.tm_year, tm.tm_mon, tm.tm_mday, tm.tm_wday,
			tm.tm_hour, tm.tm_min, tm.tm_sec);
	return 0;
}
