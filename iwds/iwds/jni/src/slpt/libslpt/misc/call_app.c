#include <call_app.h>
#include <errno.h>

static int excute_cmd(const char *path, int argc, char **argv) {
	return 0;
}

int call_app(struct app_struct *app, int argc, char **argv) {
	if (!app) {
		return -EINVAL;
	}

	if (!(app->type == 1 || app->type == 2)) {
		return -ENODEV;
	}

	if (app->type == 1) {
		return app->desc.func ? app->desc.func(argc, argv) : 0;
	}

	if (app->type == 2) {
		return app->desc.cmd ? excute_cmd(app->desc.cmd, argc, argv) : 0;
	}

	return 0;
}
