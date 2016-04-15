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

#include <utils/log.h>
#include <utils/assert.h>

#include <utils/android/property.h>

using std::string;

using Iwds::Assert;
using Iwds::Log;


static struct libcutils
{
    typedef int (*property_get_t)(const char *, char *, const char *);
    typedef int (*property_set_t)(const char *, const char *);

    void *handle;
    property_get_t func_property_get;
    property_set_t func_property_set;

    libcutils()
    {
        handle = dlopen("/system/lib/libcutils.so", RTLD_NOW);
        if (handle == NULL)
            handle = dlopen("/system/lib64/libcutils.so", RTLD_NOW);

        Assert::dieIf(
                handle == NULL,
                string("Failed to link libcutils.so."));

        func_property_get =
                (property_get_t)dlsym(handle, "property_get");

        Assert::dieIf(
                func_property_get == NULL,
                string("Failed to find symbol: property_get."));

        func_property_set =
                (property_set_t)dlsym(handle, "property_set");

        Assert::dieIf(
                func_property_set == NULL,
                string("Failed to find symbol: property_set."));
    }

    ~libcutils()
    {
        if (handle)
            dlclose(handle);
    }

    int property_get(const char *key, char *value, const char *defValue)
    {
        return func_property_get(key, value, defValue);
    }

    int property_set(const char *key, const char *value)
    {
        return func_property_set(key, value);
    }

} the_lib;

namespace Iwds
{
    string Property::getProperty(const string &key, const string &defValue)
    {
        Assert::dieIf(
                key.length() > PROPERTY_KEY_MAX - 1,
                "Key is empty or length > 31");

        Assert::dieIf(
                defValue.length() > PROPERTY_VALUE_MAX - 1,
                "Default value length > 91");

        char buffer[PROPERTY_VALUE_MAX];

        the_lib.property_get(key.data(), buffer, defValue.data());

        return string(buffer);
    }

    bool Property::setProperty(const string &key, const string &value)
    {
        Assert::dieIf(
                key.empty() || key.length() > PROPERTY_KEY_MAX - 1 ,
                "Key is empty or length > 31");

        Assert::dieIf(
                value.length() > PROPERTY_VALUE_MAX - 1,
                "Value length > 91");

        return !the_lib.property_set(key.data(), value.data());
    }
}
