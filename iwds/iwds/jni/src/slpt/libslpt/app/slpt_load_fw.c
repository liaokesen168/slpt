#include <common.h>
#include <sys/ioctl.h>
#include <slpt_ioctl.h>

int slpt_load_fw(int argc, char **argv)
{
	int err;

	if (argc != 1) {
		pr_err("slpt_load_fw: invalid args.\n");
		pr_err("       usage: slpt-linux load_fw\n");
		return -EINVAL;
	}

	err = slpt_ioctl_load_default_firmware();
	if (err < 0) {
		pr_err("load firmware failed: %s\n", strerror(errno));
		goto out;
	}

	err = slpt_ioctl_enable_default_task();
	if (err < 0)
		pr_err("enable task failed: %s\n", strerror(errno));


//	err = slpt_ioctl_disable_default_task();
//	if (err < 0)
//		pr_err("enable task failed\n", strerror(errno));

out:
	slpt_ioctl_close();
	return err;
}
