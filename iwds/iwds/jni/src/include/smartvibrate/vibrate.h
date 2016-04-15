/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  Elf/IDWS Project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 */

#ifndef __VIBRATE_H
#define __VIBRATE_H

/**
 * Name of the vibrate_module_info
 */
#define VIBRATE_MODULE_INFO_SYM         HMI

/**
 * Name of the vibrate_module_info as a string
 */
#define VIBRATE_MODULE_INFO_SYM_AS_STR  "HMI"

struct vibrate_module_t {
    /* init vibrate device */
    int (*init)(void);
    int (*special_vibrate)(int *p, int len);
};

extern struct vibrate_module_t *getVibrateModule(void);

#endif
