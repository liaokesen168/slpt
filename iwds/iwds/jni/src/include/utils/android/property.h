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


#ifndef ANDROID_PROPERTY_H
#define ANDROID_PROPERTY_H

#include <string>

#define PROPERTY_KEY_MAX    (32)
#define PROPERTY_VALUE_MAX  (92)

namespace Iwds {
    class Property
    {
    public:
        static std::string getProperty(
                const std::string &key,
                const std::string &defValue = std::string(""));

        static bool setProperty(
                    const std::string &key, const std::string &value);
    };

}

#endif
