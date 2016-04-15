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


#ifndef CRC32_H
#define CRC32_H


#include <endian.h>


typedef unsigned char  u8;
typedef unsigned short u16;
typedef unsigned int   u32;


namespace Iwds
{
    u32 crc32_le(u32 crc, unsigned char const *p, u32 len);
    u32 crc32_be(u32 crc, unsigned char const *p, u32 len);

    /**
     * crc32_le_combine - Combine two crc32 check values into one. For two
     *            sequences of bytes, seq1 and seq2 with lengths len1
     *            and len2, crc32_le() check values were calculated
     *            for each, crc1 and crc2.
     *
     * @crc1: crc32 of the first block
     * @crc2: crc32 of the second block
     * @len2: length of the second block
     *
     * Return: The crc32_le() check value of seq1 and seq2 concatenated,
     *     requiring only crc1, crc2, and len2. Note: If seq_full denotes
     *     the concatenated memory area of seq1 with seq2, and crc_full
     *     the crc32_le() value of seq_full, then crc_full ==
     *     crc32_le_combine(crc1, crc2, len2) when crc_full was seeded
     *     with the same initializer as crc1, and crc2 seed was 0. See
     *     also crc32_combine_test().
     */
    u32 crc32_le_shift(u32 crc, u32 len);

    inline u32 crc32_le_combine(u32 crc1, u32 crc2, u32 len2)
    {
        return crc32_le_shift(crc1, len2) ^ crc2;
    }

    u32 __crc32c_le(u32 crc, unsigned char const *p, u32 len);

    /**
     * __crc32c_le_combine - Combine two crc32c check values into one. For two
     *           sequences of bytes, seq1 and seq2 with lengths len1
     *           and len2, __crc32c_le() check values were calculated
     *           for each, crc1 and crc2.
     *
     * @crc1: crc32c of the first block
     * @crc2: crc32c of the second block
     * @len2: length of the second block
     *
     * Return: The __crc32c_le() check value of seq1 and seq2 concatenated,
     *     requiring only crc1, crc2, and len2. Note: If seq_full denotes
     *     the concatenated memory area of seq1 with seq2, and crc_full
     *     the __crc32c_le() value of seq_full, then crc_full ==
     *     __crc32c_le_combine(crc1, crc2, len2) when crc_full was
     *     seeded with the same initializer as crc1, and crc2 seed
     *     was 0. See also crc32c_combine_test().
     */
    u32 __crc32c_le_shift(u32 crc, u32 len);

    inline u32 __crc32c_le_combine(u32 crc1, u32 crc2, u32 len2)
    {
        return __crc32c_le_shift(crc1, len2) ^ crc2;
    }

    inline u32 crc32(u32 crc, unsigned char const *p, u32 len)
    {
        return crc32_le(crc, p, len);
    }
}

#endif
