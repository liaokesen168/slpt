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


#ifndef IWDS_ASSERT_H
#define IWDS_ASSERT_H

#include <string>

#include <utils/log.h>

using Iwds::Log;

namespace Iwds
{
    class Assert
    {
    public:
        static void dieIf(bool condition, const std::string &message)
        {
            if (!condition)
                return;

            dieIf(condition, message.c_str());
        }

        static void dieIf(bool condition, const char *message)
        {
            if (!condition)
                return;

            Log::e("Assert",
                    "================== IWDS Native Assert Failed =================");

            if (message)
                Log::e("Assert", "%s%s", "Message: ", message);

            Log::e("Assert",
                    "==================== IWDS Native Assert End ==================");


            *(volatile int *)0 = 0;
        }
    };
}

#endif
