#ifndef _SLPT_FILE_H_
#define _SLPT_FILE_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <common.h>
#include <list.h>
#include <time.h>

#define SLPT_RES_ROOT "/sys/slpt/apps/slpt-app/res/"

struct slpt_file {
	const char *fn;
	char *buf;
	unsigned int size;
	time_t mtime;
	int update;
};

static inline void slpt_file_init_status(struct slpt_file *file, const char *fn, time_t time) {
	memset(file, 0, sizeof(*file));

	file->fn = fn;
	file->mtime = time;
}

extern int slpt_load_file(struct slpt_file *file);
extern int slpt_load_file_to_mem(const char *fn, void *buf, time_t *mtime);
extern int slpt_load_file_to_mem_char(const char *fn, void *buf, time_t *mtime);

extern char *strctoc(char *s, char c1, char c2);
extern void *strtail(const char *front, const char *dst);

extern int slpt_load_single_file(const char *fn, void *buf, time_t *mtime, unsigned int rm_n);

extern int slpt_write_file(struct slpt_file *file);

#ifdef __cplusplus
}
#endif
#endif /* _SLPT_FILE_H_ */
