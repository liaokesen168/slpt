/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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

#ifndef ERRORS_H
#define ERRORS_H

#include <sys/types.h>
#include <errno.h>

namespace Iwds
{
    #ifdef HAVE_MS_C_RUNTIME
    typedef int status_t;
    #else
    typedef int32_t status_t;
    #endif

    #ifdef _WIN32
    #undef NO_ERROR
    #endif

    enum
    {
        OK = 0,
        NO_ERROR = 0,

        UNKNOWN_ERROR = 0x80000000,

        NO_MEMORY = -ENOMEM,
        INVALID_OPERATION = -ENOSYS,
        BAD_VALUE = -EINVAL,
        BAD_TYPE = 0x80000001,
        NAME_NOT_FOUND = -ENOENT,
        PERMISSION_DENIED = -EPERM,
        NO_INIT = -ENODEV,
        ALREADY_EXISTS = -EEXIST,
    #if !defined(HAVE_MS_C_RUNTIME)
        BAD_INDEX = -EOVERFLOW,
        NOT_ENOUGH_DATA = -ENODATA,
        WOULD_BLOCK = -EWOULDBLOCK,
        TIMED_OUT = -ETIMEDOUT,
        UNKNOWN_TRANSACTION = -EBADMSG,
    #else
        BAD_INDEX = -E2BIG,
        NOT_ENOUGH_DATA = 0x80000003,
        WOULD_BLOCK = 0x80000004,
        TIMED_OUT = 0x80000005,
        UNKNOWN_TRANSACTION = 0x80000006,
    #endif
    };

    #ifdef _WIN32
    #define NO_ERROR 0L
    #endif

}

#endif //ERRORS_H
