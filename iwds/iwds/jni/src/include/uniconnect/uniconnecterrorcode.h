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


#ifndef UNICONNECTERRORCODE_H
#define UNICONNECTERRORCODE_H

enum UniconnectErrorCode
{
    ENOERROR                       = 0,
    ELINKUNBOND                    = -1,
    ELINKDISCONNECTED              = -2,
    EPORTBUSY                      = -3,
    EREMOTEEXCEPTION               = -4,
    EPORTCLOSED                    = -5,
    EPORTDISCONNECTED              = -6,
    ETIMEOUT                       = -9,
};

inline const char* uniconnectErrorCodeToString(int errorCode)
{
    switch (errorCode) {
    case ENOERROR:
        return "no error";

    case ELINKUNBOND:
        return "link unbonded";

    case ELINKDISCONNECTED:
        return "link disconnected";

    case EPORTBUSY:
        return "port busy";

    case EREMOTEEXCEPTION:
        return "remote exception";

    case EPORTCLOSED:
        return "port closed";

    case EPORTDISCONNECTED:
        return "port disconnected";

    case ETIMEOUT:
        return "timeout";
    }

    return "Implement me";
}

#endif
