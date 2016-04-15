/*
 * find_pictures_test.h
 *
 *  Created on: 2015-4-7
 *      Author: LiZidong<zidong.li@ingenic.com>
 */

#ifndef SLPT_FIND_PICTURES_H_
#define SLPT_FIND_PICTURES_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <list.h>
#include <picture.h>

#define DIRECTORY_PICTURES      "/sys/slpt/apps/slpt-app/res/pictures"
#define MAX_DIR_LENGTH          255

struct grp_info {
    char *name;
    unsigned int num;
    struct list_head link;
    struct list_head handlers;
};

struct pic_info {
    char *name;
    unsigned int size;
    struct list_head link;
};

extern int do_find_pictures(void);

#ifdef __cplusplus
}
#endif
#endif /* FIND_PICTURES_TEST_H_ */
