#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <fcntl.h>
#include <file_ops.h>
#include <picture.h>

#define SET_PIC_DIR "/sys/slpt/apps/slpt-app/res/pictures/"

static int slpt_set_pic_inner(int argc, char **argv, int no_bmp)
{
	int ret;
	unsigned int length = 0;
	char cur_main_dir[MAX_FILE_NAME] = SET_PIC_DIR;
	char *buf;

	if(argc != 3) {
		pr_info(" the number of your param is wrong\n\n");
		return 0;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	/* the second param the dir we want to load, like "x.bmp" */
	if (!(buf = load_file(argv[2], NULL, 0, &length))) {
		pr_err("slpt: failed to load: %s \n", argv[2]);
		return -1;
	}

	ret = chdir(SET_PIC_DIR);
	if (ret) {
		pr_err(" (%s) change dir failed\n", SET_PIC_DIR);
		return ret;
	}

	ret = chdir(argv[1]); /* the first param is the dir we want to set, like "large_nums/0" */
	if (ret) {
		pr_err(" set %s/data failed\n", argv[1]);
		return ret;
	}

	if (no_bmp)
		ret = write_bmp_by_picture_header("data", buf);
	else
		ret = write_file("data", buf, length);

	if (ret) {
		pr_err(" (%s) write data failed\n", argv[1]);
		return ret;
	}

	free(buf);

	chdir(cur_main_dir);

	return 0;
}

static int slpt_set_picgrp_num_inner(const char *set_dir, const char *load_dir, const char *the_pic_num, int no_bmp) {
	int ret = 0;
	unsigned int length = 0;
	char *buf;
	char pic_tmp[MAX_FILE_NAME];
	char cur_load_dir[MAX_FILE_NAME];

	strcpy(cur_load_dir, load_dir);
	getcwd(cur_load_dir, MAX_FILE_NAME);

	ret = chdir(load_dir);
	if (ret) {
		pr_err(" (%s) change dir failed\n", load_dir);
		return ret;
	}

	strcpy(pic_tmp, the_pic_num);
	strcat(pic_tmp, ".bmp");
	if (!(buf = load_file(pic_tmp, NULL, 0, &length))) { /* load_one_file param enter like "0.bmp" or "1.bmp"... */
		pr_err("slpt: failed to read: %s/data\n", pic_tmp);
		ret = -1;
		goto go_return;
	}

	ret = chdir(SET_PIC_DIR);
	if (ret) {
		pr_err(" (%s) write data failed\n", SET_PIC_DIR);
		goto go_return;
	}

	ret = chdir(set_dir);
	if (ret) {
		pr_err(" set_dir %s failed\n", set_dir);
		goto go_return;
	}

	ret = chdir(the_pic_num);
	if (ret) {
		pr_err(" load %s/data failed\n", the_pic_num);
		goto go_return;
	}

	if (no_bmp)
		ret = write_bmp_by_picture_header("data", buf);
	else
		ret = write_file("data", buf, length);

	if (ret) {
		pr_err(" (%s) write data failed\n", set_dir);
		goto go_return;
	}

go_return:
	free(buf);
	chdir(cur_load_dir);
	return ret;
}

static int slpt_set_picgrp_inner(int argc, char **argv, int no_bmp)
{
	int i;
	char cur_main_dir[MAX_FILE_NAME] = SET_PIC_DIR;
	char load_the_file[][2] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	if(argc != 3) {
		pr_info(" the number of your param is wrong\n\n");
		return 0;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	/* the first param is the dir we set, like "large_nums/" */
	/* the second param is the dir we load, like "slptclock2/" */
	for(i = 0; i <= 9; i++) {
		if(slpt_set_picgrp_num_inner(argv[1], argv[2], load_the_file[i], no_bmp)) {
			pr_err(" slpt_set_pic load_the_file[%d] : %s failed\n", i, load_the_file[i]);
		}
	}

	chdir(cur_main_dir);
	return 0;
}

int slpt_set_pic(int argc, char **argv)
{
	return slpt_set_pic_inner(argc, argv, 0);
}

int slpt_set_picgrp(int argc, char **argv)
{
	return slpt_set_picgrp_inner(argc, argv, 0);
}

int slpt_set_bmp(int argc, char **argv)
{
	return slpt_set_pic_inner(argc, argv, 1);
}

int slpt_set_bmp_grp(int argc, char **argv)
{
	return slpt_set_picgrp_inner(argc, argv, 1);
}
