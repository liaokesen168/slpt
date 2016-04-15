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


#ifndef CONNECTIONMANAGER_H
#define CONNECTIONMANAGER_H

#include <string.h>

#include <string>
#include <tr1/memory>

#include <utils/protectedmap.h>
#include <utils/mutex.h>
#include <utils/condition.h>

struct Package;
class IORequest;

class Connection;
class Link;

class ConnectionManager
{
public:
    ConnectionManager(Link *link);
    ~ConnectionManager();

    std::tr1::shared_ptr<Connection> createConnection(
                                const std::string &userName,
                                pid_t userPid, Iwds::no_port_t port);

    void destroyConnection(
                        std::tr1::shared_ptr<Connection> connection);

    std::tr1::shared_ptr<Connection> findConnectionByPort(
                                                Iwds::no_port_t port);

    int enqueueIORequest(std::tr1::shared_ptr<IORequest> request);

    bool processArrivedPackage(Package *package);

    int getMaxPayloadSize() const;

    Link *getLink() const;

    std::string errorString() const;

private:
    void setErrorString(const std::string &errorString);

    // port ---> connection
    Iwds::ProtectedMap<Iwds::no_port_t,
                    std::tr1::shared_ptr<Connection> > m_connectionMap;

    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;

    Link *m_link;

private:
    // Disable copy and assign
    ConnectionManager(const ConnectionManager &obj);
    ConnectionManager &operator=(const ConnectionManager &rhs);
};


#endif
