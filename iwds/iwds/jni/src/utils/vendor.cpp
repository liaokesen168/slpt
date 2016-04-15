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


#include <string>
#include <fstream>
#include <sstream>

#include <utils/vendor.h>


using std::ifstream;
using std::string;

namespace Iwds
{
    bool Vendor::isXburstPlatform()
    {
        ifstream cpuInfo;
        cpuInfo.open("/proc/cpuinfo");
        if (!cpuInfo)
            return false;

        std::stringstream buffer;
        buffer << cpuInfo.rdbuf();
        cpuInfo.close();

        std::string contents(buffer.str());
        if (contents.find("Ingenic Xburst") == string::npos)
            return false;

        return true;
    }

    bool Vendor::isMtkPlatform()
    {
        ifstream mtkbt;
        mtkbt.open("/system/bin/mtkbt");
        if (!mtkbt)
            return false;

        mtkbt.close();

        return true;
    }
}
