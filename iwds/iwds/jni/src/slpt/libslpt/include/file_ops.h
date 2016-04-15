#ifndef _FILE_OPS_H_
#define _FILE_OPS_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <asm/errno.h>
#include <common.h>
#include <list.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <fcntl.h>

extern int access_files(char **files, int count, unsigned int mode, int *rets);
extern int open_files(char **files, int count, unsigned int mode, int *fds);
extern void close_files(int *fds, int count);

extern int read_fd(int fd, char *buf, unsigned int sz, unsigned int *_sz);
extern void *load_file(const char *fn, char *data, int sz, unsigned *_sz);
extern ssize_t write_file_by_fd(int fd, const char *buf, ssize_t size);
extern int write_file(const char *path, const char *buf, ssize_t size);

extern int get_file_mtime(const char *fn, time_t *mtime);
extern int get_fd_mtime(int fd, time_t *mtime);

struct rdfile;
extern struct rdfile *rdfile_alloc(const char *fn, int sz);
extern void rdfile_free(struct rdfile *file);
extern char *rdfile_buf(struct rdfile *file);
extern int rdfile_size(struct rdfile *file);
extern char *rdfile_name(struct rdfile *file);
extern int is_rdfile_name(struct rdfile *file, const char *name);
extern int rdfile_update(struct rdfile *file, int sz);

#ifdef __cplusplus
}
#endif
#endif /* _FILE_OPS_H_ */
