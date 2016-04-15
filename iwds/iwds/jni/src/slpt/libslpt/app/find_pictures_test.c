/*
 * find_pictures_test.c
 *
 * This source file provide the function to find pictures
 * in the <DIRECTORY_PICTURES> directory.
 *
 * The directory include two layers, first layer names like
 * "clock", "large_nums", "small_nums"...,
 * second layer names like "0", "1", "2"...
 *
 * To see more informations, you can read view/picture.c
 *
 *  Created on: 2015-4-7
 *      Author: LiZidong<zidong.li@ingenic.com>
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>
#include <common.h>
#include <find_pictures_test.h>

/*
 * The main function, you can run it by use the command "slpt find_pictures"
 */
int find_pictures_test_main(int argc, char **argv)
{
    do_find_pictures();

    return 0;
}

