/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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
 *
 */


#include <dlfcn.h>
#include <errno.h>

#include <string>

#include <utils/log.h>
#include <utils/assert.h>

#include <androidhal/hardware.h>


using std::string;

using Iwds::Log;
using Iwds::Assert;


static struct libhardware
{
    typedef int (*hw_get_module_t)(
            const char *, const struct hw_module_t **);

    typedef int (*hw_get_module_by_class_t)(
            const char *, const char *, const struct hw_module_t **);

    void *handle = 0;
    hw_get_module_t func_hw_get_module = 0;
    hw_get_module_by_class_t func_hw_get_module_by_class = 0;

    libhardware()
    {
        handle = dlopen("/system/lib/libhardware.so", RTLD_NOW);
        if (handle == NULL)
            handle = dlopen("/system/lib64/libhardware.so", RTLD_NOW);

        Assert::dieIf(
                handle == NULL,
                string("Failed to link libhardware.so."));

        func_hw_get_module =
                (hw_get_module_t)dlsym(handle, "hw_get_module");

        Assert::dieIf(
                func_hw_get_module == NULL,
                string("Failed to find symbol: hw_get_module."));

        func_hw_get_module_by_class =
                (hw_get_module_by_class_t)dlsym(
                        handle, "hw_get_module_by_class");

        Assert::dieIf(
                func_hw_get_module_by_class == NULL,
                string("Failed to find symbol: hw_get_module_by_class."));
    }

    ~libhardware()
    {
        if (handle)
            dlclose(handle);
    }

    int hw_get_module(
                    const char *id, const struct hw_module_t **module)
    {
        return func_hw_get_module(id, module);
    }

    int hw_get_module_by_class(
            const char *class_id, const char *inst,
                                    const struct hw_module_t **module)
    {
        return func_hw_get_module_by_class(class_id, inst, module);
    }

} the_lib;


int hw_get_module(const char *id, const struct hw_module_t **module)
{
    return the_lib.func_hw_get_module(id, module);
}

int hw_get_module_by_class(
        const char *class_id, const char *inst,
                                    const struct hw_module_t **module)
{
    return the_lib.hw_get_module_by_class(class_id, inst, module);
}
