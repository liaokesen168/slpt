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


#ifndef UNICONNECT_H
#define UNICONNECT_H

#include <iwds.h>

#include <uniconnect/link.h>

/*
 * Data path
 */

long long createConnectionByPort(
        const std::string &userName, int userPid,
        const std::string &address, Iwds::no_port_t port);

long long createConnectionByUuid(
        const std::string &userName, int userPid,
        const std::string &address, const std::string &uuid);

void destroyConnection(
        const std::string &address, Iwds::no_port_t port);

int read(
        const std::string &address, Iwds::no_port_t port,
        char *buffer, int offset, int maxSize);

int write(
        const std::string &address, Iwds::no_port_t port,
        char *buffer, int offset, int maxSize);

int available(
        const std::string &address, Iwds::no_port_t port);

int getMaxPayloadSize(
        const std::string &address, Iwds::no_port_t port);

int handshake(
        const std::string &address, Iwds::no_port_t port);

/*
 * Control path
 */
bool initUniconnect();

std::string getLinkManagerErrorString();

bool bondAddress(
        const std::string &linkTag, const std::string &address);

void unbondAddress(
        const std::string &address);

std::string getLinkTypes();

void setLinkStateChangedHandler(LinkStateChangedHandler *handler);

bool startServer(
        const std::string &linkTag);

void stopServer(
        const std::string &linkTag);

std::string getRemoteAddress(
        const std::string &linkTag);

#endif
