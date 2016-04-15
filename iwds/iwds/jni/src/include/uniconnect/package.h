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


#ifndef PACKAGE_H
#define PACKAGE_H

#include <endian.h>

#include <tr1/memory>
#include <memory>

#include <iwds.h>


enum PkgType {
    SYNC,
    FIN,
    DATA,

    ACK_SYNC,
    ACK_FIN,
    ACK_DATA,

    NACK_SYNC,

    NACK_LINK
};

struct PackageHeader
{
#if __BYTE_ORDER == __LITTLE_ENDIAN
    Iwds::u8 type;
    Iwds::u8 reserved;

#elif __BYTE_ORDER == __BIG_ENDIAN
    #error "Do not support big-endian system."
#endif

    Iwds::no_port_t port;

    /*
     * ACKed bytes count if type == PkgType::ACK
     */
    Iwds::size_pkg_t dataLength;

    void reset()
    {
        type = 0;
        reserved = 0;
        port = 0;
        dataLength = 0;
    }

    void dump()
    {
        const char *TAG = "PackageHeader";

        Iwds::Log::i(TAG, "============ Package Header ============");
        Iwds::Log::i(TAG, "Type: %s", typeToString(type));
        Iwds::Log::i(TAG, "Port: %u", port);
        Iwds::Log::i(TAG, "Data length: %d", dataLength);
        Iwds::Log::i(TAG, "========================================");

    }

    static const char *typeToString(int type)
    {
        switch (type)
        {
        case SYNC:
            return "SYNC";

        case FIN:
            return "FIN";

        case DATA:
            return "DATA";

        case ACK_SYNC:
            return "ACK_SYNC";

        case ACK_FIN:
            return "ACK_FIN";

        case ACK_DATA:
            return "ACK_DATA";

        case NACK_SYNC:
            return "NACK_SYNC";

        case NACK_LINK:
            return "NACK_LINK";

        default:
            Iwds::Assert::dieIf(true, "Implement me.");

            break;
        }

        return "Implement me.";
    }

} __packed;

struct Package
{
    static std::tr1::shared_ptr<Package> createPackage(
                        Iwds::no_port_t port, enum PkgType type,
                            char *data = 0, Iwds::size_pkg_t size = 0)
    {
        Iwds::Assert::dieIf(size > 0 && !data, "Data is null.");

        Package *pkg = new Package();

        pkg->header.type = type;
        pkg->header.reserved = 0;
        pkg->header.port = port;
        pkg->header.dataLength = size;
        pkg->data = data;

        return std::tr1::shared_ptr<Package>(pkg);;
    }

    void reset()
    {
        header.reset();
        data = 0;
    }

    Package() :
        header({0, 0, 0, 0}),
        data(0)
    {

    }

    PackageHeader header;
    char *data;
};

#endif
