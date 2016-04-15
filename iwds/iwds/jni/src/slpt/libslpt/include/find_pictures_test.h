/*
 * find_pictures_test.h
 *
 *  Created on: 2015-4-7
 *      Author: LiZidong<zidong.li@ingenic.com>
 */

#ifndef FIND_PICTURES_TEST_H_
#define FIND_PICTURES_TEST_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <slpt_find_pictures.h>

extern struct list_head pictures_list;

extern int find_pictures_test_main(int argc, char **argv);

#ifdef __cplusplus
}
#endif
#endif /* FIND_PICTURES_TEST_H_ */
