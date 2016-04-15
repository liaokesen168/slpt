#ifndef _CALL_APP_H_
#define _CALL_APP_H_
#ifdef __cplusplus
extern "C" {
#endif

struct app_struct {
	const char *name;
	union {
		int (*func)(int argc, char **argv);
		const char *cmd;
	} desc;
	unsigned int type;
};

#define APP_FUNC(index, str, f) [index] = {.name = str, .desc.func = f, .type = 1,}
#define APP_BIN(index, str, path) [index] = {.name = str, .desc.cmd = path, .type = 2,}

extern int call_app(struct app_struct *app, int argc, char **argv);

#ifdef __cplusplus
}
#endif
#endif /* _CALL_APP_H_ */
