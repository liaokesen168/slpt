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
#include <tr1/memory>

#include <iwds.h>

#include <utils/assert.h>
#include <utils/log.h>
#include <utils/crc32.h>

#include <uniconnect/link.h>
#include <uniconnect/connection.h>
#include <uniconnect/connectionmanager.h>
#include <uniconnect/linkmanager.h>
#include <uniconnect/uniconnecterrorcode.h>

#include <uniconnect/uniconnect.h>

using std::string;
using std::tr1::shared_ptr;

using Iwds::crc32;
using Iwds::u8;
using Iwds::no_port_t;

using Iwds::Assert;
using Iwds::Log;

static shared_ptr<LinkManager> lm = LinkManager::getInstance();

static const string LOG_TAG("Uniconnect");

long long createConnectionByPort(
        const string &userName, int userPid,
        const string &address, no_port_t port)
{
    IWDS_TRACE;

    Log::i(LOG_TAG.c_str(),
            "Create for user: %s, PID: %d, port: %u...",
            userName.c_str(), userPid, port);

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link) {
        Log::e(LOG_TAG.c_str(),
                "Failed to create for"
                " user: %s, PID: %d, port: %u, error: %s.",
                userName.c_str(), userPid, port,
                uniconnectErrorCodeToString(
                        UniconnectErrorCode::ELINKUNBOND));

        return UniconnectErrorCode::ELINKUNBOND;
    }

    if (!link->isConnected()) {
        Log::e(LOG_TAG.c_str(),
                "Failed to create for"
                " user: %s, PID: %d, port: %u, error: %s.",
                userName.c_str(), userPid, port,
                uniconnectErrorCodeToString(
                        UniconnectErrorCode::ELINKDISCONNECTED));

        return UniconnectErrorCode::ELINKDISCONNECTED;
    }

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->createConnection(
                                userName, pid_t(userPid), port);
    if (!conn) {
        Log::e(LOG_TAG.c_str(),
                "Failed to create for"
                " user: %s, PID: %d, port: %u, error: %s.",
                userName.c_str(), userPid, port,
                uniconnectErrorCodeToString(
                        UniconnectErrorCode::EPORTBUSY));

        return UniconnectErrorCode::EPORTBUSY;
    }

    Log::i(LOG_TAG.c_str(),
            "Success to create for user: %s, PID: %d, port: %u",
            userName.c_str(), userPid, port);

    return 0;
}


long long createConnectionByUuid(
        const string &userName, int userPid,
        const string &address, const string &uuid)
{
    IWDS_TRACE;

    no_port_t port = uuid == "{this-is-god-master}" ?
                    0 : crc32(0, (u8 *)uuid.c_str(), uuid.length());

    long long error = createConnectionByPort(
                                    userName, userPid, address, port);
    if (error)
        return error;

    return port;
}

void destroyConnection(const string &address, no_port_t port)
{
    IWDS_TRACE;

    Log::i(LOG_TAG.c_str(), "Destroy port: %u", port);

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link) {
        Log::e(LOG_TAG.c_str(),
                "Failed to destroy"
                " port: %u, error: %s(address: %s).",
                port,
                uniconnectErrorCodeToString(
                        UniconnectErrorCode::ELINKUNBOND),
                address.c_str());
        return;
    }

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->findConnectionByPort(port);
    if (!conn) {
        Log::e(LOG_TAG.c_str(),
                "Failed to destroy for"
                " port: %u, error: %s.",
                port,
                uniconnectErrorCodeToString(
                        UniconnectErrorCode::EPORTCLOSED));
        return;
    }

    link->getConnectionManager()->destroyConnection(conn);

    Log::i(LOG_TAG.c_str(),
            "Success to destroy for"
            " user: %s, PID: %d, port: %u",
            conn->getUserName().c_str(),
            conn->getUserPid(),
            conn->getPort());
}

int getMaxPayloadSize(const string &address, no_port_t port /* Unused */)
{
    IWDS_TRACE;

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link)
        return UniconnectErrorCode::ELINKUNBOND;

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    return cm->getMaxPayloadSize();
}

int read(
        const string &address, no_port_t port,
        char *buffer, int offset, int maxSize)
{
    IWDS_TRACE;

    if (maxSize <= 0)
        return 0;

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link)
        return UniconnectErrorCode::ELINKUNBOND;

    if (!link->isConnected())
        return UniconnectErrorCode::ELINKDISCONNECTED;

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->findConnectionByPort(port);
    if (!conn)
        return UniconnectErrorCode::EPORTCLOSED;

    buffer += offset;

    return conn->read(buffer, maxSize);
}

int write(
        const string &address, no_port_t port,
        char *buffer, int offset, int maxSize)
{
    IWDS_TRACE;

    if (maxSize <= 0)
        return 0;

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link)
        return UniconnectErrorCode::ELINKUNBOND;

    if (!link->isConnected())
        return UniconnectErrorCode::ELINKDISCONNECTED;

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->findConnectionByPort(port);
    if (!conn)
        return UniconnectErrorCode::EPORTCLOSED;

    buffer += offset;

    return conn->write(buffer, maxSize);
}

int available(const string &address, no_port_t port)
{
    IWDS_TRACE;

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link)
        return UniconnectErrorCode::ELINKUNBOND;

    if (!link->isConnected())
        return UniconnectErrorCode::ELINKDISCONNECTED;

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->findConnectionByPort(port);
    if (!conn)
        return UniconnectErrorCode::EPORTCLOSED;

    return conn->available();
}

int handshake(const string &address, no_port_t port)
{
    IWDS_TRACE;

    shared_ptr<Link> link = lm->findLinkByAddress(address);
    if (!link)
        return UniconnectErrorCode::ELINKUNBOND;

    if (!link->isConnected())
        return UniconnectErrorCode::ELINKDISCONNECTED;

    shared_ptr<ConnectionManager> cm = link->getConnectionManager();

    shared_ptr<Connection> conn = cm->findConnectionByPort(port);
    if (!conn)
        return UniconnectErrorCode::EPORTCLOSED;

    return conn->connect();
}

bool initUniconnect()
{
    IWDS_TRACE;

    return lm->initialize();
}

string getLinkManagerErrorString()
{
    IWDS_TRACE;

    return lm->errorString();
}

bool bondAddress(const string &linkTag, const string &address)
{
    IWDS_TRACE;

    return lm->bond(linkTag, address);
}

void unbondAddress(const string &address)
{
    IWDS_TRACE;

    lm->unbond(address);
}

string getLinkTypes()
{
    IWDS_TRACE;

    return lm->getLinkTypes();
}

void setLinkStateChangedHandler(LinkStateChangedHandler *handler)
{
    IWDS_TRACE;

    lm->setStateChangedHandler(handler);
}

bool startServer(const string &linkTag)
{
    IWDS_TRACE;

    return lm->startServer(linkTag);
}

void stopServer(const string &linkTag)
{
    IWDS_TRACE;

    lm->stopServer(linkTag);
}

string getRemoteAddress(const string &linkTag)
{
    IWDS_TRACE;

    return lm->getRemoteAddress(linkTag);
}


