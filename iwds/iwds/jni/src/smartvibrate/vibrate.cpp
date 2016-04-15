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

#include <dlfcn.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>
#include <limits.h>

#include <iwds.h>

#include <smartvibrate/vibrate.h>

using std::tr1::shared_ptr;
using std::string;

using Iwds::Log;
using Iwds::Assert;

static const string LOG_TAG("VibrateModule: ");

#define LIBRARY_FILE "/system/lib/hw/haptic.watch.so"

struct vibrate_module_t *getVibrateModule(void)
{
    int status;
    void *handle;
    struct vibrate_module_t *hmi;

    const char *sym = VIBRATE_MODULE_INFO_SYM_AS_STR;

    /*
     * load the symbols resolving undefined symbols before
     * dlopen returns. Since RTLD_GLOBAL is not or'd in with
     * RTLD_NOW the external symbols will not be global
     */
    handle = dlopen(LIBRARY_FILE, RTLD_NOW);
    if (handle == NULL) {
        char const *err_str = dlerror();
        Log::e(LOG_TAG.c_str(), "load: module=%s\n%s",
                LIBRARY_FILE, err_str?err_str:"unknown");
        status = -EINVAL;
        goto done;
    }

    /* Get the address of the struct hal_module_info. */
    hmi = (struct vibrate_module_t *) dlsym(handle, sym);
    if (hmi == NULL) {
        Log::e(LOG_TAG.c_str(), "load: couldn't find symbol %s", sym);
        status = -EINVAL;
        goto done;
    }

    /* success */
    status = 0;

done:
    if (status != 0) {
        hmi = NULL;
        if (handle != NULL) {
            dlclose(handle);
            handle = NULL;
        }
    } else {
        Log::e(LOG_TAG.c_str(), "loaded HAL path=%s hmi=%p handle=%p",
                LIBRARY_FILE, *hmi, handle);
    }

    return hmi;
}

