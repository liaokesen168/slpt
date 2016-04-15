#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <fcntl.h>
#include <file_ops.h>
#include <view.h>

#define VIEW_TOP_DIR "/sys/slpt/apps/slpt-app/res/clock/"

int slpt_set_view_int(int argc, char **argv)
{
	int ret;
	char enter_dir[MAX_FILE_NAME], set_dir[MAX_FILE_NAME];
	int write_data;
	int i;
	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;
	char cur_enter_dir[MAX_FILE_NAME];
	if(argc < 4) {
		pr_info(" your param too few\n\n");
		return 0;
	}

	if((argc - 2) % 2 != 0) {
		pr_err("your param is invalid\n");
		goto fail_return;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(VIEW_TOP_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", VIEW_TOP_DIR);
		return 0;
	}

	memcpy(enter_dir, argv[1], strlen(argv[1]) + 1); /* the first param is like "anaglog_clock/hour/" */
	ret = chdir(enter_dir);
	if (ret) {
		pr_err(" changed dir (%s) failed\n", enter_dir);
		goto fail_return;
	}

	for(i = 2; i < argc; i += 2) {
		getcwd(cur_enter_dir, MAX_FILE_NAME);

		memcpy(set_dir, argv[i], strlen(argv[i]) + 1); /* the second param is like "center-x" */
		ret = chdir(set_dir);
		if (ret) {
			pr_err(" changed dir (%s) failed\n", set_dir);
			goto fail_return;
		}

		write_data = strtol(argv[i+1], '\0', 0);
		if(write_data == 0) {
			pr_info("the data you write is 0,\nif not, maybe you write wrong param cause it !\n");
		}

		ret = write_file("data", (char *)&write_data, sizeof(int)); /* write a int */
		if(ret) {
			pr_err(" (%s) write data failed\n", argv[i]);
			goto fail_write;
		}

		pr_info("(%s) write_data is(%d)\n", argv[i], write_data);

fail_write :
		ret = chdir(cur_enter_dir);
		if (ret) {
			pr_err(" changed dir (%s) failed\n", cur_enter_dir);
			goto fail_return;
		}
	}

fail_return :
	chdir(cur_main_dir); /* return  */
	return 0;
}

int slpt_set_view_str(int argc, char **argv)
{
	int ret;
	int i;
	char enter_dir[MAX_FILE_NAME], set_dir[MAX_FILE_NAME];
	int data_count;
	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;
	char cur_enter_dir[MAX_FILE_NAME];

	if(argc < 4) {
		pr_info(" your param too few\n\n");
		return 0;
	}

	if((argc - 2) % 2 != 0) {
		pr_err("your param is invalid\n");
		goto fail_return;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(VIEW_TOP_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", VIEW_TOP_DIR);
		return 0;
	}

	memcpy(enter_dir, argv[1], strlen(argv[1]) + 1);  /* the first param is like "anaglog_clock/hour/" */
	ret = chdir(enter_dir);
	if (ret) {
		pr_err(" changed dir (%s) failed\n", enter_dir);
		goto fail_return;
	}

	for(i = 2; i < argc; i += 2) {
		getcwd(cur_enter_dir, MAX_FILE_NAME);

		memcpy(set_dir, argv[i], strlen(argv[i]) + 1);    /* the second param is like "picture" */
		ret = chdir(set_dir);
		if (ret) {
			pr_err(" changed dir (%s) failed\n", set_dir);
			goto fail_return;
		}

		data_count = strlen(argv[i+1]) + 1; /* the third param is anything string, the max length is 60 */

		ret = write_file("data", argv[i+1], data_count);
		if (ret) {
			pr_err(" (%s) write data failed\n", argv[i]);
			goto fail_write;
		}

		pr_info("(%s) write_data is(%s)\n", argv[i], argv[i+1]);

fail_write :
		ret = chdir(cur_enter_dir);
		if (ret) {
			pr_err(" changed dir (%s) failed\n", cur_enter_dir);
			goto fail_return;
		}
	}

fail_return :
	chdir(cur_main_dir); /* return  */
	return 0;
}

extern int default_pictures_init_onetime(void);
extern int slpt_load_fb_region(struct fb_region *region);

int slpt_init_view_state(int argc, char **argv)
{
	unsigned int type;
	struct view *view;
	struct fb_region region;

	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;

	if (argc != 3) {
		pr_err("init view state: Invalid args !(eg. slpt init_view view_type view_name)\n");
		return 0;
	}

	if (slpt_load_fb_region(&region)) {
		pr_err("init view state: failed to load fb_region \n");
		return 0;
	}

	set_current_fb_region(&region);

	type = name_to_view_type(argv[1]);
	if (type >= VIEW_NUMS) {
		pr_err("init view state: Invalid view type (%s)\n", argv[1]);
		return 0;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(VIEW_TOP_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", VIEW_TOP_DIR);
		return 0;
	}

	if (chdir(argv[2])) {
		pr_err("init view state: Can not find view (%s)\n", argv[2]);
		goto fail_return;
	}

	chdir("..");

	default_pictures_init_onetime();

	view = alloc_view_by_type(argv[2], type);
	if (view == NULL) {
		pr_err("init view state: Can not support this kind of view (%s)\n", argv[1]);
		goto fail_return;
	}

	/* sync to view */

	slpt_write_view(view);

fail_return:
	chdir(cur_main_dir);
	return 0;
}
