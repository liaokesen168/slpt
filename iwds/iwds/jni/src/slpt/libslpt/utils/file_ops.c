#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <fcntl.h>
#include <utime.h>

#include <asm/errno.h>
#include <common.h>
#include <list.h>
#include <file_ops.h>

int get_file_mtime(const char *fn, time_t *mtime) {
	struct stat st;
	int ret;

	ret = stat(fn, &st);
	if (ret) {
		pr_err("stat: failed to stat file: %s\n", fn);
		return ret;
	}

	*mtime = st.st_mtime;
	return 0;
}

int get_fd_mtime(int fd, time_t *mtime) {
	struct stat st;
	int ret;

	ret = fstat(fd, &st);
	if (ret) {
		pr_err("stat: failed to stat file: %d\n", fd);
		return ret;
	}

	*mtime = st.st_mtime;
	return 0;
}

int read_fd(int fd, char *buf, unsigned int sz, unsigned int *_sz) {
	ssize_t nr  = 0;
	unsigned int len = 0;

	while (sz != 0) {
		nr = read(fd, buf, sz);
		if (!nr) {
			break;
		} else if (nr < 0) {
			break;
		}
		len += nr;
		sz -= nr;
		buf += nr;
	}
	if (nr < 0)
		return nr;

	if (_sz) {
		*_sz = len;
	}

	return 0;
}

/**
 * load_file - load a file(@sz bytes), the last character will be set to zero,
 *
 * @fn : file path
 * @data : buf to load file, if null malloc @sz bytes to load file
 * @sz: buf size (we will load @sz bytes), if zero will be set to file_size,
 * @_sz : if not null, return the real size load from file
 *
 * return_val: return filebuf if success, zero if failed
 */
void *load_file(const char *fn, char *data, int sz, unsigned *_sz)
{
	unsigned int nr = 0;
	int fd;
	int alloc = 0;

	fd = open(fn, O_RDONLY);
	if(fd < 0)
		return 0;

	if (sz <= 0) {
		sz = lseek(fd, 0, SEEK_END);
		if(sz < 0)
			goto oops;
		if(lseek(fd, 0, SEEK_SET) != 0)
			goto oops;
	}

	if (data == 0) {
		data = (char*) malloc(sz);
		if(data == 0)
			goto oops;
		alloc = 1;
	}

	if (!_sz)
		_sz = &nr;

	if (read_fd(fd, data, sz, _sz) < 0)
		goto oops;

	close(fd);
	/* data[*_sz] = 0; */

	return data;
oops:
	close(fd);
	if(alloc != 0 && data != 0)
		free(data);
	return 0;
}

ssize_t write_file_by_fd(int fd, const char *buf, ssize_t size) {
	ssize_t nw, len = 0;

	while (size > 0) {
		nw = size;
		nw = write(fd, buf, nw);
		if (nw <= 0)
			break;
		size -= nw;
		buf += nw;
		len += nw;
	}

	return len;
}

/**
 * write_file - write bytes to a file
 *
 * @path : file path
 * @buf : buf to be write
 * @size : bytes to be write
 */
int write_file(const char *path, const char *buf, ssize_t size) {
	int fd;
	int ret = 0;

	if (!path || !buf)
		return -EINVAL;

	fd = open(path, O_WRONLY | O_SYNC);
	if (fd < 0)
		return fd;

	if (write_file_by_fd(fd, buf, size) < size) {
		ret = -1;
	}

	close(fd);

	utime(path, NULL);

	return ret;
}

int access_files(char **files, int count, unsigned int mode, int *rets) {
	int i = 0;
	int ret = 0;
	int nerr = 0;

	if (!files)
		return -EINVAL;

	for (i = 0; i < count; ++i) {
		ret = access(files[i], mode);
		if (ret) {
			pr_err("access: %s access error\n", files[i]);
			nerr++;
		}
		if (rets)
			rets[i] = ret;
	}

	return nerr;
}

int open_files(char **files, int count, unsigned int mode, int *fds) {
	int i;
	int fd = 0;
	int nerr = 0;

	if (!files || !fds)
		return -EINVAL;

	for (i = 0; i < count; ++i) {
		fd = open(files[i], mode);
		if (fd < 0) {
			nerr++;
			pr_err("access: %s open error\n", files[i]);
		}
		fds[i] = fd;
	}

	return nerr;
}

void close_files(int *fds, int count) {
	int i;

	if (!fds)
		return;

	for (i = 0; i < count; ++i) {
		if (fds[i] >= 0)
			close(fds[i]);
	}
}

struct rdfile {
	char *name;
	char *buf;
	int fd;
	int size;
	time_t mtime;

	struct {
		unsigned int inited:1;
	} s;
};

static inline void rdfile_init(struct rdfile *file) {
	file->name = 0;
	file->buf = 0;
	file->fd = -1;
	file->size = 0;
	file->mtime = 0;
	file->s.inited = 1;
	file->mtime = 0;
}


/* @sz is need to read from sysfile, because it can't get real size by lseek().
 *   ls -l sysfile_binary: the size is 0
 *   ls -l sysfile_general: the size is 4096
 */
struct rdfile *rdfile_alloc(const char *fn, int sz) {
	int ret;
	char *buf;
	unsigned int nr;
	unsigned int len;
	struct rdfile *file;

	if (!fn) {
		pr_err("rdfile: %s args not valid\n", __FUNCTION__);
		return ERR_PTR(-EINVAL);
	}

	if (access(fn, F_OK)) {
		pr_err("rdfile: file (%s) not exist\n", fn);
		return ERR_PTR(-ENOENT);
	}

	if (access(fn, R_OK)) {
		pr_err("rdfile: file (%s) has no read permission\n", fn);
		return ERR_PTR(-ENOENT);
	}

	file = malloc(sizeof(*file));
	if (!file) {
		pr_err("rdfile: failed to alloc struct\n");
		return ERR_PTR(-ENOMEM);
	}

	rdfile_init(file);

	len = strlen(fn) + 1;
	file->name = malloc(len);
	if (!file->name) {
		pr_err("rdfile: failed to alloc file name\n");
		ret = -ENOMEM;
		goto free_file;
	}
	memcpy(file->name, fn, len);

	ret = get_file_mtime(fn, &file->mtime);
	if (ret) {
		pr_err("rdfile: failed to get file mtime\n");
		goto free_name;
	}

	buf = load_file(file->name, NULL, sz, &nr);
	if (!buf) {
		ret = -ENOMEM;
		goto free_name;
	}

	file->buf = buf;
	file->size = nr;

	return file;
free_name:
	free(file->name);
free_file:
	free(file);
	return ERR_PTR(ret);
}

static void rdfile_free_internal(struct rdfile *file) {
	if (file->buf)
		free(file->buf);

	if (file->fd >= 0)
		close(file->fd);

	if (file->name)
		free(file->name);
}

void rdfile_free(struct rdfile *file) {
	if (!file) {
		pr_err("rdfile: %s args not valid\n", __FUNCTION__);
		return;
	}

	rdfile_free_internal(file);

	free(file);
}

char *rdfile_buf(struct rdfile *file) {
	if (!file && !file->s.inited)
		return NULL;

	return file->buf;
}

int rdfile_size(struct rdfile *file) {
	if (!file && !file->s.inited)
		return -1;

	return file->size;
}

char *rdfile_name(struct rdfile *file) {
	if (!file && !file->s.inited)
		return NULL;

	return file->name;
}

int is_rdfile_name(struct rdfile *file, const char *name) {
	if (!file && !file->s.inited)
		return 0;

	return !strcmp(file->name, name);
}

/* @sz is need to read from sysfile, because it can't get real size by lseek().
 *   ls -l sysfile_binary: the size is 0
 *   ls -l sysfile_general: the size is 4096
 */
int rdfile_update(struct rdfile *file, int sz) {
	char *buf = NULL;
	time_t mtime;
	int ret;
	unsigned int nr;

	if (!file || !file->s.inited) {
		pr_err("rdfile: %s args not inited\n", __FUNCTION__);
		return -EINVAL;
	}

	ret = get_file_mtime(file->name, &mtime);
	if (ret) {
		pr_err("rdfile: failed to get mtime of file (%s)\n", file->name);
		return ret;
	}

	if (file->mtime != mtime)
		buf = load_file(file->name, NULL, sz, &nr);
	else
		return 1;

	if (!buf)
		return -ENOMEM;

	if (file->buf)
		free(file->buf);
	file->buf = buf;
	file->size = nr;

	return 0;
}
