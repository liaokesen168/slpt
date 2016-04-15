#include <common.h>
#include <slpt_file.h>
#include <view.h>
#include <digital_clock.h>
#include <rotate_pic.h>
#include <analog_clock.h>
#include <file_ops.h>

int slpt_load_file_internal(const char *fn, char **base, unsigned int *nr) {
	char buf[20];
	unsigned int size;
	char *p;
	int ret;
	unsigned int nrr = 0;

	if (!load_file("length", buf, 20, NULL)) {
		pr_err("slpt: failed to read: %s/data\n", fn);
		return ret;
	}
	sscanf(buf, "%d", &size);

	pr_debug("%s size is %d\n", fn, size);

	p = load_file("data", NULL, size, &nrr);
	if (!p) {
		pr_err("slpt: failed to read: %s/data\n", fn);
		return ret;
	}

	*base = p;
	*nr = nrr;

	return 0;
}

int slpt_load_file(struct slpt_file *file) {
	time_t mtime = 0;
	int ret;
	char *buf;
	unsigned int size;

	ret = chdir(file->fn);

	if (ret) {
		pr_err("load: failed to change dir: %s\n", file->fn);
		return ret;
	}

	ret = get_file_mtime("data", &mtime);
	if (ret) {
		pr_err("load: failed to get mtime: %s", file->fn);
		goto back_to_dir;
	}
	if (file->mtime != mtime) {
		ret = slpt_load_file_internal(file->fn, &buf, &size);
		if (ret) {
			pr_err("load: failed to load file: %s", file->fn);
			goto back_to_dir;
		}
		if (file->buf)
			free(file->buf);
		file->buf = buf;
		file->size = size;
		file->mtime = mtime;
		file->update = 1;
	}

back_to_dir:
	chdir("..");
	return ret;
}

int slpt_write_file(struct slpt_file *file) {
	int ret;

	ret = chdir(file->fn);
	if (ret) {
		pr_err("write: failed to change dir: %s\n", file->fn);
		return ret;
	}

	ret = write_file("data", file->buf, file->size);
	if(ret) {
		pr_err(" (%s) write data failed\n", file->fn);
		goto back_to_dir;
	}

back_to_dir:
	chdir("..");
	return ret;
}

int slpt_load_single_file(const char *fn, void *buf, time_t *mtime, unsigned int rm_n) {
	int ret;
	char cur_dir[MAX_FILE_NAME] = SLPT_RES_ROOT;

	getcwd(cur_dir, MAX_FILE_NAME);
	ret = chdir("/sys/slpt/apps/slpt-app/res/");
	if (ret) {
		pr_err("slpt_load_single_file: changed dir failed\n");
		return -1;
	}

	if (rm_n)
		ret = slpt_load_file_to_mem_char(fn, buf, mtime);
	else
		ret = slpt_load_file_to_mem(fn, buf, mtime);

	chdir(cur_dir);

	return ret;
}

extern int default_pictures_init_onetime(void);
extern void slpt_sync_pictures(void);
extern void slpt_load_analog_clock(struct analog_clock *clock);
extern void slpt_display_sync(void);

int view_test_main(int argc, char **argv) {
	struct analog_clock clocken;
	int ret;

	default_pictures_init_onetime();

	ret = chdir("/sys/slpt/apps/slpt-app/res/");
	if (ret) {
		pr_err("view test: changed dir failed\n");
		return 0;
	}
	chdir("clock/");

	if (init_analog_clock(&clocken, "analog-clocka")) {
		pr_err("test: failed to init analog clock \n");
		return 0;
	}

	slpt_load_analog_clock(&clocken);

	slpt_sync_pictures();


	slpt_display_sync();

	return 0;
}
