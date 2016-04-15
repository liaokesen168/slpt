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


#ifndef IWDS_LOG_H
#define IWDS_LOG_H

#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>

#include <string>

#include <android/log.h>


#define LOG_BUF_SIZE        1024
#define LOG_TAG_PREFIX      "IWDS_NATIVE---"

namespace Iwds
{
    class Log
    {
    public:
        static void d(const std::string &tag, const std::string &message);
        static void v(const std::string &tag, const std::string &message);
        static void i(const std::string &tag, const std::string &message);
        static void w(const std::string &tag, const std::string &message);
        static void e(const std::string &tag, const std::string &message);

        /*
         * For c-style string
         */
        static void d(const char *tag, const char *fmt, ...);
        static void v(const char *tag, const char *fmt, ...);
        static void i(const char *tag, const char *fmt, ...);
        static void w(const char *tag, const char *fmt, ...);
        static void e(const char *tag, const char *fmt, ...);

        static void setDebugEnabled(bool enable);
        static bool isDebugEnabled();

    private:
        static bool sm_isDebugEnabled;
    };
}

#endif
