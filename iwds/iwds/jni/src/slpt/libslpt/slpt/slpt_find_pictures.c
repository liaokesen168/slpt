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
#include <slpt_find_pictures.h>

//#define DEBUG_FIND_PICTURES
#if defined(DEBUG_FIND_PICTURES)
#define pr_dbg(fmt, args...)    pr_info("%s(): " fmt, __func__, ##args)
#else
#define pr_dbg(fmt, args...)
#endif

LIST_HEAD(pictures_list);
static struct list_head tmp_pictures_list;

static int list_dir(char *path, struct list_head *head);
static int list_pic(struct list_head *head);
static int store_grp_info(const char *name, struct list_head *head);
static int store_pic_info(const char *name, struct grp_info *g);
static int store_tmp_pictures_list(void);
static int re_add_picture_grp(struct grp_info *g, struct picture_grp *pic_g);

static int do_compare_pictures(struct pic_info *p_info, struct picture_grp *pic_grp);
static int compare_groups(void);
static int compare_pictures_by_group(struct grp_info *g_info, struct picture_grp *pic_grp);
static int compare_picture_groups(void);

static int show_all_pictures(void);
static int show_pic_by_grp(struct picture_grp *pic_g);

static void free_grps_and_pics(void);
static void free_pics(struct grp_info *g_info);

int do_find_pictures(void)
{
    char cur_dir[MAX_FILE_NAME];

    getcwd(cur_dir, MAX_FILE_NAME);

	if(chdir(DIRECTORY_PICTURES) != 0) {
		pr_err("Couldn`t change (%s) diretory!", DIRECTORY_PICTURES);
		return -1;
	}

    list_dir(DIRECTORY_PICTURES, &pictures_list);
    list_pic(&pictures_list);
    compare_picture_groups();

    show_all_pictures();
    free_grps_and_pics();

    chdir(cur_dir);
    return 0;
}
/*
 * To list the first layer, and store to pictures_list
 */
static int list_dir(char *path, struct list_head *head)
{
    DIR *ptr_dir = NULL;
    struct dirent *dir_entry = NULL;

    if (path == NULL || head == NULL)
        return -1;

    ptr_dir = opendir(path);
    if (ptr_dir == NULL) {
        pr_info("Can not open directory %s\n", path);
        return -1;
    }

    pr_dbg("Open directory %s successful\n", path);
    while ((dir_entry = readdir(ptr_dir)) != NULL) {
        if (strcmp(dir_entry->d_name, ".") == 0 ||
                strcmp(dir_entry->d_name, "..") == 0) {
            continue;
        }

        if (dir_entry->d_type & DT_DIR) {
            store_grp_info(dir_entry->d_name, head);
        }
    }

    closedir(ptr_dir);
    return 0;
}

/*
 * To list the first layer, and store to grp_info.handlers
 */
static int list_pic(struct list_head *head)
{
    struct list_head *pos = NULL;
    struct grp_info *g = NULL;
    DIR *ptr_dir = NULL;
    struct dirent *dir_entry = NULL;
    int num = 0;

    if (head == NULL)
        return -1;

    list_for_each(pos, head) {
        num = 0;
        g = list_entry(pos, struct grp_info, link);
        ptr_dir = opendir(g->name);
        while ((dir_entry = readdir(ptr_dir)) != NULL) {
            if (strcmp(dir_entry->d_name, ".") == 0 ||
                    strcmp(dir_entry->d_name, "..") == 0) {
                continue;
            }

            if (dir_entry->d_type & DT_DIR) {
                num++;
                store_pic_info(dir_entry->d_name, g);
                g->num = num;
            }
        }
        closedir(ptr_dir);
    }
    return 0;
}

/**
 * store_grp_info - store the picture group's informations.
 */
static int store_grp_info(const char *name, struct list_head *head)
{
    struct grp_info *info = NULL;

    if (name == NULL || head == NULL)
        return -1;

    info = malloc(sizeof(struct grp_info));
    if (info == NULL) {
        pr_info("%s : allocate memory failed!\n", __FUNCTION__);
        return -1;
    }
    memset(info, 0, sizeof(struct grp_info));

    INIT_LIST_HEAD(&info->handlers);

    info->name = malloc(sizeof(char) * MAX_DIR_LENGTH);
    if (info->name == NULL) {
        pr_info("%s %d: allocate memory failed!\n", __FUNCTION__, __LINE__);
        free(info);
        return -1;
    }
    strcpy(info->name, name);

    list_add_tail(&info->link, head);
    return 0;
}

/**
 * store_pic_info - store the picture's informations.
 */
static int store_pic_info(const char *name, struct grp_info *g)
{
    struct list_head *head = NULL;
    struct pic_info *info = NULL;
    FILE *fp = NULL;
    char file_name[MAX_DIR_LENGTH];

    if (name == NULL || g == NULL)
        return -1;

    head = &g->handlers;
    info = malloc(sizeof(struct pic_info));
    if (info == NULL) {
        pr_info("%s %d: allocate memory failed!\n", __FUNCTION__, __LINE__);
        return -1;
    }
    memset(info, 0, sizeof(struct pic_info));

    info->name = malloc(sizeof(char) * MAX_DIR_LENGTH);
    if (info->name == NULL) {
        pr_info("%s %d: allocate memory failed!\n", __FUNCTION__, __LINE__);
        free(info);
        return -1;
    }
    strcpy(info->name, name);
    sprintf(file_name, "%s/%s/%s/length",DIRECTORY_PICTURES, g->name, name);
    pr_dbg("file_name = %s\n", file_name);

    fp = fopen(file_name, "r+");
    if (fp == NULL) {
        pr_info("Open the file \"length\" failed\n");
        return -1;
    }
    fscanf(fp, "%d", &info->size);
    fclose(fp);

    pr_dbg("name %s,size %d\n", info->name, info->size);
    list_add_tail(&info->link, head);
    return 0;
}

/*
 * re_add_picture_grp - remove the old struct picture_grp,
 * and add struct grp_info to new picture_grp.
 *
 * Note: @pic_g can set to NULL
 */
static int re_add_picture_grp(struct grp_info *g, struct picture_grp *pic_g)
{
    struct list_head *pos = NULL;
    struct pic_info *pic_i = NULL;

    if (g == NULL) {
        return -1;
    }

    if (pic_g != NULL) {
        free_picture_grp(pic_g);
    }

    if (g->num == 0) {  /*There is not pictures in group, just create group*/
        pr_dbg("Group \"%s\" has no pictures\n", g->name);
        alloc_picture_grp(g->name, NULL, 0);
    } else {
        list_for_each(pos, &g->handlers) {
            pic_i = list_entry(pos, struct pic_info, link);
            alloc_picture2(g->name, pic_i->name, pic_i->size);
        }
    }

    return 0;
}

/**
 * compare_picture_groups - Compare find_pictures' groups if correct.
 *
 * return the numbers modify struct picture_grp.
 */
static int compare_picture_groups(void)
{
    int ret = 0;

    ret = store_tmp_pictures_list();
    if (ret != 0)
        return ret;

    compare_groups();

    return 0;
}

static int store_tmp_pictures_list(void)
{
	list_cut_position(&tmp_pictures_list, &picture_grp, picture_grp.prev);
    return 0;
}

static int compare_groups(void)
{
    struct list_head *pos = NULL, *pos2 =NULL;
    struct list_head *n = NULL, *n2 = NULL;
    struct picture_grp *pic_grp = NULL;
    struct grp_info *g_info = NULL;
    int found = 0;
    int ret = 0;

    list_for_each_safe(pos, n, &pictures_list) {
        g_info = list_entry(pos, struct grp_info, link);
        found = 0;

        list_for_each_safe(pos2, n2, &tmp_pictures_list) {
            pic_grp = list_entry(pos2, struct picture_grp, link);

            if (!strcmp(g_info->name, pic_grp->name)) {     //Find the group
                found = 1;
                pr_dbg("%s : Find %s\n", __FUNCTION__, g_info->name);
                list_move_tail(pos2, &picture_grp);
                compare_pictures_by_group(g_info, pic_grp);
                break;
            }
        }

        if (found == 0) {   //Have not find
            pr_info("%s : Not find %s\n", __FUNCTION__, g_info->name);
            ret = re_add_picture_grp(g_info, NULL);
            if (ret != 0) {
                pr_info("%s : Failed to create & alloc picture_grp\n", __FUNCTION__);
                return ret;
            }
        }
    }

    return 0;
}

static int compare_pictures_by_group(struct grp_info *g_info, struct picture_grp *pic_grp)
{
    int ret = 0;
    int cmp = 0;
    struct list_head *pos = NULL;
    struct pic_info *p_info = NULL;

    if (g_info == NULL || pic_grp == NULL)
        return -1;

    if (g_info->num != pic_grp->size) {
        ret = re_add_picture_grp(g_info, NULL);
        if (ret != 0)
            return ret;
    } else {
        if (g_info->num == 0)
            return 0;

        list_for_each(pos, &g_info->handlers) {
            p_info = list_entry(pos, struct pic_info, link);
            cmp = do_compare_pictures(p_info, pic_grp);
            if (cmp != 0) {
                re_add_picture_grp(g_info, pic_grp);
                break;
            }
        }
    }

    return 0;
}

static int do_compare_pictures(struct pic_info *p_info, struct picture_grp *pic_grp)
{
    unsigned int i = 0;

    if (p_info == NULL || pic_grp == NULL)
        return -1;

    for (i = 0; i < pic_grp->size; i++) {
        if (!strcmp(p_info->name, pic_grp->array[i]->name)) {
            if (p_info->size == pic_grp->array[i]->size) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    return -1;
}

/**
 * show_all_pictures - show all the pictures that we found
 */
static int show_all_pictures(void)
{
    struct list_head *pos = NULL;
    struct picture_grp *pic_g = NULL;

    pr_info("Show directory %s:\n", DIRECTORY_PICTURES);

    list_for_each(pos, &picture_grp) {
        pic_g = list_entry(pos, struct picture_grp, link);
        show_pic_by_grp(pic_g);
    }

    return 0;
}

static int show_pic_by_grp(struct picture_grp *pic_g)
{
    unsigned int i = 0;

    if (pic_g == NULL)
        return -1;

    pr_info("%s[%d]\n", pic_g->name, pic_g->size);
    for (i = 0; i < pic_g->size; i++) {
        pr_info("|______%s    %d\n", pic_g->array[i]->name, pic_g->array[i]->size);
    }
    pr_info("\n");

    return 0;
}

/**
 * free_grps_and_pics - free all the memories that allocated to find pictures.
 */
static void free_grps_and_pics(void)
{
    struct list_head *pos = NULL, *n = NULL;
    struct grp_info *g_info = NULL;

    list_for_each_safe(pos, n, &pictures_list) {
        g_info = list_entry(pos, struct grp_info, link);
        free_pics(g_info);
        list_del(&g_info->link);
        free(g_info->name);
        free(g_info);
    }
}

static void free_pics(struct grp_info *g_info)
{
    struct list_head *pos = NULL, *n = NULL;
    struct pic_info *p_info = NULL;

    if (g_info == NULL)
        return;

    list_for_each_safe(pos, n, &g_info->handlers) {
        p_info = list_entry(pos, struct pic_info, link);
        list_del(&p_info->link);
        free(p_info->name);
        free(p_info);
    }
}

