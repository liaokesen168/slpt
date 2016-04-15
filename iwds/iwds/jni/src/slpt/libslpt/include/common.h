#ifndef _COMMON_H_
#define _COMMON_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <unistd.h>
#include <assert.h>
#include <errno.h>
#include <stddef.h>
#include <android/log.h>
#include <jni.h>

#ifndef LOGI
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#endif
#ifndef LOGD
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#endif
#ifndef LOGE
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)
#endif

#ifndef container_of
/**
 * container_of - cast a member of a structure out to the containing structure
 * @ptr:	the pointer to the member.
 * @type:	the type of the container struct this is embedded in.
 * @member:	the name of the member within the struct.
 *
 */
#define container_of(ptr, type, member) ({			\
	const typeof(((type *)0)->member) * __mptr = (ptr);	\
	(type *)((char *)__mptr - offsetof(type, member)); })
#endif

#define min(X, Y)				\
	({ typeof(X) __x = (X);			\
		typeof(Y) __y = (Y);		\
		(__x < __y) ? __x : __y; })

#define max(X, Y)				\
	({ typeof(X) __x = (X);			\
		typeof(Y) __y = (Y);		\
		(__x > __y) ? __x : __y; })

#ifndef PTR_ERR
#define MAX_ERRNO	4095
#define IS_ERR(x)  (((unsigned long)(x)) >= ((unsigned long)-MAX_ERRNO))
#define PTR_ERR(x)  ((unsigned long)(x))
#define ERR_PTR(x)  ((void *) (long )(x))
#endif

#undef LOG_TAG
#define LOG_TAG "slpt"

#ifndef pr_info
#define pr_info(x...) LOGD(x)
#define pr_err(x...) LOGE(x)
#ifdef DEBUG
#define PR_DEBUG 1
#else
#define PR_DEBUG 0
#endif
#define pr_debug(x...)							\
	do {										\
		if (PR_DEBUG)							\
			pr_info(x);							\
	} while (0)
#endif

#undef ALIGN
#define ALIGN(x, a)	(((x) + (a) - 1) & ~((a) - 1))

#ifndef ARRAY_SIZE
#define ARRAY_SIZE(a) (sizeof(a) / sizeof((a)[0]))
#endif

static inline void ndk_assert(int ret) {
	assert(ret);
}

#undef assert
#define assert(expr)                                                    \
do {                                                                    \
    if (!(long ) (expr)) {                                             \
        pr_err("===================================================");  \
        pr_err("BUG: %s %s %d\n", __FILE__, __FUNCTION__, __LINE__);    \
        pr_err("===================================================");  \
        ndk_assert(0);                                                  \
    }                                                                   \
} while (0)

#define MAX_FILE_NAME 1024


/* we are in slpt linux */
#define CONFIG_SLPT_LINUX 1

extern void *malloc_with_name(unsigned int size, const char *name);

/* determine 32 bits or 64 bits system */
static inline int system_type_bits() {
	void *p;
	return sizeof(p);
}

/* jlong to address */
static inline void *jlong_to_address(jlong val) {
	unsigned long address;

	address = (unsigned long) (unsigned long long) val;

	return (void *)address;
}

/* address to jlong  */
static inline jlong address_to_jlong(void *p) {
	unsigned long address;

	address = (unsigned long)p;

	return (unsigned long long)address;
}

#ifdef __cplusplus
}
#endif
#endif /* _COMMON_H_ */
