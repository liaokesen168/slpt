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
 *
 *
 * ======================================================
 *  !!! I say: This implementation borrow many codes !!!
 *  !!!        from linux project                    !!!
 *  !!!                                              !!!
 *  !!!        Thank you for their excellent work.   !!!
 * ======================================================
 *
 *
 */


#ifndef CRC16_H
#define CRC16_H

#include <iwds.h>

namespace Iwds
{
    /**
     * crc16 - compute the CRC-16 for the data buffer
     * @crc:    previous CRC value
     * @buffer: data pointer
     * @len:    number of bytes in the buffer
     *
     * Returns the updated CRC value.
     */
    u16 crc16(u16 crc, u8 const *buffer, unsigned int len);
}

#endif
