#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <fcntl.h>
#include <file_ops.h>
#include <view.h>

#include <slpt_file.h>

#define VIEW_TOP_DIR "/sys/slpt/apps/slpt-app/res/clock/"

extern int slpt_load_file(struct slpt_file *file);

int slpt_load_buf(const char *fn, void *buf)
{
	int ret;
	struct slpt_file file ;

	memset(&file, 0, sizeof(file));
	file.fn = fn;

	ret = slpt_load_file(&file);
	if (ret) {
		pr_err("slpt: failed to load view: %s\n", file.fn);
		return ret;
	}

	memcpy(buf, file.buf, file.size);
	free(file.buf);

	return 0;
}

int slpt_get_view_int(int argc, char **argv)
{
	int ret;
	char enter_dir[MAX_FILE_NAME];
	unsigned int load_data;
	int i;
	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;
	char cur_enter_dir[MAX_FILE_NAME];

	if(argc < 3) {
		pr_info(" your param too few\n\n");
		return 0;
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

	for(i = 2; i < argc; i++) {
		getcwd(cur_enter_dir, MAX_FILE_NAME);

		ret = slpt_load_buf(argv[i], &load_data); /* the second param is like "center-x" */
		if (ret) {
			pr_err(" slpt_load_buf (%s) failed\n", argv[i]);
			goto fail_load;
		}

		pr_info("(%s) load_data is(%d)\n", argv[i], load_data);
		printf("(%s) load_data is(%d)\n", argv[i], load_data);

fail_load :
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

int slpt_get_view_str(int argc, char **argv)
{
	int ret = 0;
	int i;
	char enter_dir[MAX_FILE_NAME];
	char load_data[MAX_FILE_NAME];
	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;
	char cur_enter_dir[MAX_FILE_NAME];

	if(argc < 3) {
		pr_info(" your param too few\n\n");
		return 0;
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

	for(i = 2; i < argc; i++) {
		getcwd(cur_enter_dir, MAX_FILE_NAME);

		ret = slpt_load_buf(argv[i], load_data); /* the second param is like "picture" */
		if (ret) {
			pr_err(" slpt_load_buf (%s) failed\n", argv[i]);
			goto fail_load;
		}

		pr_info("(%s) load_data is(%s)\n", argv[i], load_data);
		printf("(%s) load_data is(%s)\n", argv[i], load_data);

fail_load :
		ret = chdir(cur_enter_dir);
		if (ret) {
			pr_err(" changed dir (%s) failed\n", cur_enter_dir);
			goto fail_return;
		}
	}

fail_return :
	chdir(cur_main_dir); /* return  */
	return ret;
}
